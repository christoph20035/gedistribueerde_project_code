import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Client {
    private List<DataFriend> friends = new ArrayList<> ();
    private String name;
    private BulletinBoard bulletinBoard;
    private SecureRandom secureRandom = new SecureRandom();
    private int BULLETIN_BOARD_SIZE;

    MessageDigest digest = MessageDigest.getInstance("SHA-256");

    public Client(String name, String SERVER_NAME, int PORT_NUMBER, int BULLETIN_BOARD_SIZE) throws RemoteException, NotBoundException, NoSuchAlgorithmException {
        this.name = name;
        Registry myRegistry = LocateRegistry.getRegistry("localhost", PORT_NUMBER);
        bulletinBoard = (BulletinBoard) myRegistry.lookup(SERVER_NAME);
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

    public void sendMessage(String message, DataFriend selectedFriend) throws IOException {
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

        byte[] iv = new byte[16];
        byte[] encryptedTextAES = AES_encrypt(data, symmetric_key_write, iv);

        bulletinBoard.add(index_write, encryptedTextAES, digest.digest(tag_write));

        selectedFriend.idx_write = new_index;
        selectedFriend.tag_write = new_tag;


        System.out.println("[WARNING]: toevoegen dat de symmetrische key veranderd na dat een bericht wordt gestuurd");
        //selectedFriend.symmetricKey_write = KDF(symmetric_key_write);
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

    public void receiveMessage(DataFriend friend) throws RemoteException {
        int index = friend.idx_read;
        byte[] tag = friend.tag_read;
        bulletinBoard.get(index, tag);
    }

}

