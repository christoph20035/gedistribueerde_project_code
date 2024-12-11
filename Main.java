import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Main {
    // l -> tag moet groter zijn dan 2^l
    public static final int SECURITY_PARAMETER = 20;
    public static final int BULLETIN_BOARD_SIZE = 256;
    public static final int NUMPARTITIONS = 4;

    // De tag kan een waarde aannemen tussen de 0 en 2^(2*l)
    public static final int TAG_BITS = SECURITY_PARAMETER*2;

    public static KeyGenerator keyGenAES;

    public static final String SERVER_NAME = "BulletinBoard";
    public static final int PORT_NUMBER = 55000;


    public static void main (String[] args) throws NoSuchAlgorithmException, IOException, NotBoundException {

        // ------------------ START SERVER ------------------

        Registry registry = LocateRegistry.createRegistry(PORT_NUMBER);
        // Create an instance of your server implementation

        PartitionManager manager = new PartitionManagerImpl();
        manager.startServers(NUMPARTITIONS, BULLETIN_BOARD_SIZE, PORT_NUMBER);

        // Bind the server to a specific name
        registry.rebind(SERVER_NAME, manager);

        // -------------- ADD SERVER TO CLIENT --------------
        keyGenAES = KeyGenerator.getInstance("AES");
        keyGenAES.init(256);// 256-bit sleutel

        Client client_christoph = new Client("Christoph", SERVER_NAME, PORT_NUMBER, BULLETIN_BOARD_SIZE*NUMPARTITIONS);
        Client client_gust = new Client("Gust", SERVER_NAME, PORT_NUMBER, BULLETIN_BOARD_SIZE*NUMPARTITIONS);
        Client client_an = new Client("An", SERVER_NAME, PORT_NUMBER, BULLETIN_BOARD_SIZE*NUMPARTITIONS);

        makeFriends(client_christoph, client_gust);
        makeFriends(client_an, client_gust);

        // Launch the GUI
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI(client_christoph);
            gui.setVisible(true);
        });

        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI(client_gust);
            gui.setVisible(true);
        });

        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI(client_an);
            gui.setVisible(true);
        });
    }

    private static void makeFriends(Client clientAlice, Client clientBob) throws NoSuchAlgorithmException, IOException {
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenAES = KeyGenerator.getInstance("AES");
        keyGenAES.init(256); // 256-bit sleutel

        int index_1 = secureRandom.nextInt(BULLETIN_BOARD_SIZE); // Generate een nummer tussen 0 <= x < BULLETIN_BOARD_SIZE
        int index_2 = secureRandom.nextInt(BULLETIN_BOARD_SIZE);

        SecretKey key_1 = keyGenAES.generateKey();
        SecretKey key_2 = keyGenAES.generateKey();

        byte[] tag_1 = new byte[TAG_BITS];
        secureRandom.nextBytes(tag_1);
        byte[] tag_2 = new byte[TAG_BITS];
        secureRandom.nextBytes(tag_2);

        byte[] iv_1 = new byte[16];
        secureRandom.nextBytes(iv_1);
        byte[] iv_2 = new byte[16];
        secureRandom.nextBytes(iv_2);

        // Voor de KDF
        byte[] salt = new byte[32];
        secureRandom.nextBytes(salt);

        DataFriend df_1 = new DataFriend(index_1, index_2, tag_1, tag_2, clientBob.getName(), key_1, key_2, iv_1, iv_2, salt);
        DataFriend df_2 = new DataFriend(index_2, index_1, tag_2, tag_1, clientAlice.getName(), key_2, key_1, iv_2, iv_1, salt);

        clientAlice.addFriend(df_1);
        clientBob.addFriend(df_2);
    }
}
