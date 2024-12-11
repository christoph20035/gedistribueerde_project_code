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
import java.util.HashMap;
import java.util.Map;

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
    }
}
