import javax.swing.*;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public class ClientMain {

    public static void main(String[] args) throws NotBoundException, NoSuchAlgorithmException, RemoteException {

        if (args.length != 2) {
            System.out.println("Please provide exactly two arguments.");
            return;
        }
        String name = args[0];
        String input_path = args[1];

        Client client = new Client(name, Main.SERVER_NAME, Main.PORT_NUMBER, Main.BULLETIN_BOARD_SIZE * Main.NUMPARTITIONS, input_path);
        if (client == null) {
            System.out.println("Client " + name + " not found.");
        }
        try {
            // Step 1: Read JSON file
            Gson gson = new Gson();
            Type listType = new TypeToken<List<DataFriend>>() {}.getType();

            FileReader reader = new FileReader(input_path); // Ensure "input.json" is in the correct path
            List<DataFriend> dataFriends = gson.fromJson(reader, listType);
            reader.close();

            // Step 2: Process the DataFriend objects
            for (DataFriend dataFriend : dataFriends) {
                dataFriend.initialize();
                client.addFriend(dataFriend);
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        launchGUI(client);
    }

    public static void launchGUI(Client client) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI(client);
            gui.setVisible(true);
        });
    }


}
