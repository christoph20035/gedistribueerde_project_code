import javax.crypto.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private List<DataFriend> friends = new ArrayList<> ();
    byte[] index;
    byte[] tag;
    String name;

    public Client(String name){
        this.name = name;
    }

    public List<DataFriend> getFriends() {
        return friends;
    }

    public void addFriend(Client clientGust) {
        friends.add(new DataFriend(clientGust.getName()));
    }

    public String getName(){
        return name;
    }
}

