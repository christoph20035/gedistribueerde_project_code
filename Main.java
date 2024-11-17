import javax.swing.*;

public class Main {
    // l -> tag moet groter zijn dan 2^l
    public static final int SECURITY_PARAMETER = 20;
    public static final int BULLETIN_BOARD_SIZE = 256;

    // Aantal bits nodig om een index voor te stellen als een unsigned integer (index mag toch niet 0 zijn)
    public static final int INDEX_BITS = (int) Math.ceil(Math.log(BULLETIN_BOARD_SIZE)/Math.log(2));
    // De tag kan een waarde aannemen tussen de 2^l en 2^(2*l)
    public static final int TAG_BITS = SECURITY_PARAMETER*2;

    public static void main (String[] args) {
        System.out.println(INDEX_BITS);

        Client client_christoph = new Client("Christoph");
        Client client_gust = new Client("Gust");

        client_christoph.addFriend(client_gust);
        client_gust.addFriend(client_christoph);

        // Launch the GUI
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI(client_christoph);
            gui.setVisible(true);
        });

        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI(client_gust);
            gui.setVisible(true);
        });
    }
}
