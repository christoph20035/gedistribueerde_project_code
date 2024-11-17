import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        setTitle("Client Messaging");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

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
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        messagePanel.add(inputPanel, BorderLayout.SOUTH);
        add(messagePanel, BorderLayout.CENTER);
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

    private void sendMessage() {
        if (selectedFriend == null || messageInput.getText().isEmpty()) return;

        String message = messageInput.getText();
        /*selectedFriend.sendMessage(message); // Add to sent messages
        selectedFriend.receiveMessage("Hello"); // Simulate received response*/

        messageInput.setText("");
        updateMessageHistory();
    }

}
