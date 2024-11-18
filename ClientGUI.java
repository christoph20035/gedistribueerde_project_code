import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;

public class ClientGUI extends JFrame {
    private Client client;
    private JList<String> friendList;
    private DefaultListModel<String> friendListModel;
    private JTextArea messageHistory;
    private JTextField messageInput;
    private JButton sendButton;
    private DataFriend selectedFriend;

    public ClientGUI(Client client) {
        this.client = client;
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Client: " + client.getName());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel (for the refresh button)
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            try {
                refreshUI();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        }); // Define the refresh action
        topPanel.add(refreshButton, BorderLayout.EAST); // Place the button on the right

        add(topPanel, BorderLayout.NORTH);

        // Friend List Panel
        friendListModel = new DefaultListModel<>();
        for (DataFriend friend : client.getFriends()) {
            friendListModel.addElement(friend.name);
        }
        friendList = new JList<>(friendListModel);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.addListSelectionListener(e -> selectFriend(friendList.getSelectedValue()));

        JScrollPane friendListScrollPane = new JScrollPane(friendList);
        friendListScrollPane.setPreferredSize(new Dimension(150, 0));
        add(friendListScrollPane, BorderLayout.WEST);

        // Message Panel
        JPanel messagePanel = new JPanel(new BorderLayout());

        messageHistory = new JTextArea();
        messageHistory.setEditable(false);
        JScrollPane messageHistoryScrollPane = new JScrollPane(messageHistory);
        messagePanel.add(messageHistoryScrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageInput = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            try {
                sendMessage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        messagePanel.add(inputPanel, BorderLayout.SOUTH);
        add(messagePanel, BorderLayout.CENTER);
    }

    // Define the refreshUI method to handle the refresh action
    private void refreshUI() throws RemoteException {
        // Add logic to refresh the user interface (e.g., reload the friend list, clear message history)
        if (selectedFriend == null) return;
        client.receiveMessage(selectedFriend);
    }

    private void selectFriend(String friendName) {
        if (friendName == null) return;

        for (DataFriend friend : client.getFriends()) {
            if (friend.name.equals(friendName)) {
                selectedFriend = friend;
                break;
            }
        }
        updateMessageHistory();
    }

    private void updateMessageHistory() {
        if (selectedFriend == null) return;

        messageHistory.setText(""); // Clear previous messages
        /*for (String message : selectedFriend.getMessages()) {
            messageHistory.append(message + "\n");
        }*/
    }

    private void sendMessage() throws IOException {
        if (selectedFriend == null || messageInput.getText().isEmpty()) return;

        String message = messageInput.getText();
        /* // Add to sent messages
        selectedFriend.receiveMessage("Hello"); // Simulate received response*/
        client.sendMessage(message, selectedFriend);
        messageHistory.append(message + "\n");

        messageInput.setText("");
        updateMessageHistory();
    }

}
