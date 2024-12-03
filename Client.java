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

public class Client {
    private List<DataFriend> friends = new ArrayList<> ();
    private String name;
    private PartitionManager bulletinBoard;
    private SecureRandom secureRandom = new SecureRandom();
    private int BULLETIN_BOARD_SIZE;

    MessageDigest digest = MessageDigest.getInstance("SHA-256");

    public Client(String name, String SERVER_NAME, int PORT_NUMBER, int BULLETIN_BOARD_SIZE) throws RemoteException, NotBoundException, NoSuchAlgorithmException {
        this.name = name;
        Registry myRegistry = LocateRegistry.getRegistry("localhost", PORT_NUMBER);
        bulletinBoard = (PartitionManager) myRegistry.lookup(SERVER_NAME);
        if(bulletinBoard == null) {
            System.out.println("BulletinBoard not found");
        }

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

    public void sendMessage(String message, DataFriend selectedFriend) throws IOException, NoSuchAlgorithmException {
        int index_write = selectedFriend.idx_write;
        byte[] tag_write = selectedFriend.tag_write;
        SecretKey symmetric_key_write = selectedFriend.symmetricKey_write;


        int new_index = secureRandom.nextInt(BULLETIN_BOARD_SIZE);
        byte[] new_tag = new byte[BULLETIN_BOARD_SIZE];
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

        selectedFriend.symmetricKey_write = KDF(symmetric_key_write, selectedFriend.salt_write);
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

    public String receiveMessage(DataFriend friend) throws RemoteException, NoSuchAlgorithmException {
        int index = friend.idx_read;
        byte[] tag = friend.tag_read;
        byte[] data =  bulletinBoard.get(index, tag);
        if(data == null) {
            System.out.println("[WARNING] geen data received!");
            return "";
        }

        byte[] decryptedDataAES = AES_decrypt(data, friend.symmetricKey_read, friend.iv_read);
        if(decryptedDataAES == null) {
            System.out.println("[WARNING] Kon data niet deëncrypteren");
            return "";
        }

        //the last
        byte[] new_tag = Arrays.copyOfRange(decryptedDataAES, decryptedDataAES.length - BULLETIN_BOARD_SIZE, decryptedDataAES.length);
        byte[] new_indexB = Arrays.copyOfRange(decryptedDataAES, decryptedDataAES.length - BULLETIN_BOARD_SIZE-4, decryptedDataAES.length- BULLETIN_BOARD_SIZE);
        byte[] message = Arrays.copyOfRange(decryptedDataAES, 0, decryptedDataAES.length - BULLETIN_BOARD_SIZE-4);
        int new_index = ByteBuffer.wrap(new_indexB).getInt();
        friend.idx_read = new_index;
        friend.tag_read = new_tag;

        friend.symmetricKey_read = KDF(friend.symmetricKey_read, friend.salt_read);

        return new String(message);
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

}

