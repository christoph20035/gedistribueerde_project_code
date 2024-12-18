import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

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
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Add a WindowListener to trigger clearHistory() on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                clearHistory(); // Call your custom method
            }
        });

        initializeGUI();
    }


    private void initializeGUI() {
        setTitle("Client: " + client.getName());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel (for the refresh button)
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            try {
                refreshUI();
            } catch (NoSuchAlgorithmException | IOException ex) {
                throw new RuntimeException(ex);
            }
        }); // Define the refresh action

        JButton induceCorruptedButton = new JButton("Induce Corrupted Idx/tag");
        induceCorruptedButton.addActionListener(e -> {
            induceCorrupted();
        });

        topPanel.add(refreshButton, BorderLayout.EAST); // Place the button on the right
        topPanel.add(induceCorruptedButton, BorderLayout.WEST);
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
            } catch (IOException | NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        });

        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        messagePanel.add(inputPanel, BorderLayout.SOUTH);
        add(messagePanel, BorderLayout.CENTER);
    }

    // Define the refreshUI method to handle the refresh action
    private void refreshUI() throws IOException, NoSuchAlgorithmException {
        // Add logic to refresh the user interface (e.g., reload the friend list, clear message history)
        if (selectedFriend == null) return;
        String text = client.receiveMessage(selectedFriend);
        if(text == null){
            raiseCorruptedError("index, tag or key is corrupted");
        }
        else{
            while(!text.equals("")) {
                String receivedMessage = selectedFriend.name+": "+text+"\n";
                messageHistory.append(receivedMessage);
                selectedFriend.addMessage(receivedMessage);

                text = client.receiveMessage(selectedFriend);
            }
        }
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
        for (String message : selectedFriend.getMessageHys()) {
            messageHistory.append(message);
        }
    }

    private void sendMessage() throws IOException, NoSuchAlgorithmException {
        if (selectedFriend == null || messageInput.getText().isEmpty()) return;

        String text = client.receiveMessage(selectedFriend);
        if(text == null){
            raiseCorruptedError("index, tag or key is corrupted");
        }
        else{
            while(!text.equals("")){
                String receivedMessage = selectedFriend.name+": "+text+"\n";
                messageHistory.append(receivedMessage);
                selectedFriend.addMessage(receivedMessage);

                text = client.receiveMessage(selectedFriend);
            }
        }

        String message = messageInput.getText();
        /* // Add to sent messages
        selectedFriend.receiveMessage("Hello"); // Simulate received response*/
        if (!client.sendMessage(message, selectedFriend)) {
            raiseCorruptedError("index, tag or key is corrupted"); // Show the error popup
        }
        else{
            String sendMessage = "ik: "+message + "\n";
            messageHistory.append(sendMessage);

            messageInput.setText("");

            selectedFriend.addMessage(sendMessage);
        }
    }

    private void induceCorrupted(){
        if (selectedFriend == null) return;
        client.induceCorrupted(selectedFriend);
    }

    public void clearHistory() {
        System.out.println("Gesloten gedrukt");
        for(DataFriend friend : client.getFriends()){
            friend.clearMessageHys();
        }
        client.exportDataFriend();
        messageHistory.setText(""); // Clear the GUI message history
        System.out.println("History cleared for client: " + client.getName());
    }


    private void raiseCorruptedError(String error) {
        JOptionPane.showMessageDialog(this,
                "Corrupted state detected: " + error,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }


}
