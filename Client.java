import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;
import java.io.FileWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Base64;

public class Client {
    private List<DataFriend> friends = new ArrayList<> ();
    private String name;
    private PartitionManager bulletinBoard;
    private SecureRandom secureRandom = new SecureRandom();
    private int BULLETIN_BOARD_SIZE;
    private int TAG_BITS;
    private String export_path;

    MessageDigest digest = MessageDigest.getInstance("SHA-256");

    public Client(String name, String SERVER_NAME, int PORT_NUMBER, int BULLETIN_BOARD_SIZE, String export_path, int tagBits) throws RemoteException, NotBoundException, NoSuchAlgorithmException {
        this.name = name;
        this.export_path = export_path;
        Registry myRegistry = LocateRegistry.getRegistry("localhost", PORT_NUMBER);
        bulletinBoard = (PartitionManager) myRegistry.lookup(SERVER_NAME);
        if(bulletinBoard == null) {
            System.out.println("BulletinBoard not found");
        }
        this.TAG_BITS = tagBits;
        this.BULLETIN_BOARD_SIZE = BULLETIN_BOARD_SIZE;
    }

    public List<DataFriend> getFriends() {
        return friends;
    }

    public String getName(){
        return name;
    }

    public void addFriend(DataFriend df2) {
        friends.add(df2);
    }

    public boolean sendMessage(String message, DataFriend selectedFriend) throws IOException, NoSuchAlgorithmException {
        int index_write = selectedFriend.idx_write;
        byte[] tag_write = selectedFriend.tag_write;
        SecretKey symmetric_key_write = selectedFriend.symmetricKey_write;
        if(!checkCorrupted(index_write, tag_write, symmetric_key_write, selectedFriend, selectedFriend.hashed_state_write)){
            return false;
        }

        int new_index = secureRandom.nextInt(BULLETIN_BOARD_SIZE);
        byte[] new_tag = new byte[TAG_BITS];
        secureRandom.nextBytes(new_tag);

        // Voeg bytes achter elkaar toe om message te vormen
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(message.getBytes());
        outputStream.write(ByteBuffer.allocate(4).putInt(new_index).array());
        outputStream.write(new_tag);
        byte[] data = outputStream.toByteArray();

        byte[] encryptedTextAES = AES_encrypt(data, symmetric_key_write, selectedFriend.iv_write);

        byte[] hashed_tag = digest.digest(tag_write);

        System.out.println(new_index);

        bulletinBoard.add(index_write, encryptedTextAES, hashed_tag);

        selectedFriend.idx_write = new_index;
        selectedFriend.tag_write = new_tag;
        SecretKey newKey = KDF(symmetric_key_write, selectedFriend.salt);
        selectedFriend.symmetricKey_write = newKey;
        selectedFriend.hashed_state_write = selectedFriend.setHashedState(new_index, new_tag, newKey);
        return true;
    }

    private boolean checkCorrupted(int indexWrite, byte[] tagWrite, SecretKey symmetricKeyWrite, DataFriend selectedFriend, byte[] hashed_state) throws IOException {
        byte[] keyBytes = symmetricKeyWrite.getEncoded();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(ByteBuffer.allocate(4).putInt(indexWrite).array());
        outputStream.write(tagWrite);
        outputStream.write(keyBytes);
        byte[] state = outputStream.toByteArray();
        byte[] calculated_hashed_state = digest.digest(state);
        return Arrays.equals(calculated_hashed_state, hashed_state);
    }

    public SecretKey KDF(SecretKey symmetric_key, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = symmetric_key.getEncoded();
        digest.update(keyBytes);
        digest.update(salt);

        byte[] derivedKeyBytes = digest.digest();

        return new SecretKeySpec(derivedKeyBytes, 0, 16, symmetric_key.getAlgorithm());
    }

    public static byte[] AES_encrypt(byte[] data, SecretKey sKey, byte[] iv) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sKey, ivSpec);

            byte[] ciphertext = cipher.doFinal(data);
            return ciphertext;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String receiveMessage(DataFriend friend) throws IOException, NoSuchAlgorithmException {
        int index = friend.idx_read;
        byte[] tag = friend.tag_read;
        byte[] data =  bulletinBoard.get(index, tag);
        if(!checkCorrupted(index, tag, friend.symmetricKey_read, friend, friend.hashed_state_read)){
            return null;
        }
        if(data == null) {
            System.out.println("[WARNING] geen data received! " + name);
            return "";
        }

        byte[] decryptedDataAES = AES_decrypt(data, friend.symmetricKey_read, friend.iv_read);
        if(decryptedDataAES == null) {
            System.out.println("[WARNING] Kon data niet deëncrypteren " + name);
            return "";
        }

        //the last
        byte[] new_tag = Arrays.copyOfRange(decryptedDataAES, decryptedDataAES.length - TAG_BITS, decryptedDataAES.length);
        byte[] new_indexB = Arrays.copyOfRange(decryptedDataAES, decryptedDataAES.length - TAG_BITS-4, decryptedDataAES.length- TAG_BITS);
        byte[] message = Arrays.copyOfRange(decryptedDataAES, 0, decryptedDataAES.length - TAG_BITS-4);
        int new_index = ByteBuffer.wrap(new_indexB).getInt();
        friend.idx_read = new_index;
        friend.tag_read = new_tag;
        SecretKey newKey = KDF(friend.symmetricKey_read, friend.salt);
        friend.symmetricKey_read = newKey;
        friend.hashed_state_read = friend.setHashedState(new_index, new_tag, newKey);
        return new String(message);
    }

    public void induceCorrupted(DataFriend friend){
        friend.induceCorrupted();
    }

    public static byte[] AES_decrypt(byte[] encryptedText, SecretKey skey, byte[] iv) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec);

            return cipher.doFinal(encryptedText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void exportDataFriend() {
        Gson gson = new Gson();

        // Create a JsonArray to store the serialized DataFriend objects
        JsonArray jsonArray = new JsonArray();

        for (DataFriend df : friends) {
            JsonObject friendJson = new JsonObject();

            // Convert the required fields to Base64 strings and add to the JSON object
            friendJson.addProperty("symmetricKey_writeString", Base64.getEncoder().encodeToString(df.symmetricKey_write.getEncoded()));
            friendJson.addProperty("symmetricKey_readString", Base64.getEncoder().encodeToString(df.symmetricKey_read.getEncoded()));
            friendJson.addProperty("idx_write", df.idx_write);
            friendJson.addProperty("idx_read", df.idx_read);
            friendJson.addProperty("tag_writeString", Base64.getEncoder().encodeToString(df.tag_write));
            friendJson.addProperty("tag_readString", Base64.getEncoder().encodeToString(df.tag_read));
            friendJson.addProperty("iv_writeString", Base64.getEncoder().encodeToString(df.iv_write));
            friendJson.addProperty("iv_readString", Base64.getEncoder().encodeToString(df.iv_read));
            friendJson.addProperty("saltString", Base64.getEncoder().encodeToString(df.salt));
            friendJson.addProperty("name", df.name);
            friendJson.addProperty("hashed_state_writeString", Base64.getEncoder().encodeToString(df.hashed_state_write));
            friendJson.addProperty("hashed_state_readString", Base64.getEncoder().encodeToString(df.hashed_state_read));

            // Add the JSON object to the JsonArray
            jsonArray.add(friendJson);
        }

        // Write the JSON array to a file
        try (FileWriter writer = new FileWriter(export_path)) {
            gson.toJson(jsonArray, writer);  // Use Gson to serialize and write to file
            System.out.println("DataFriends exported successfully to: " + export_path);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

}

