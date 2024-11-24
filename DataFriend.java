import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

public class DataFriend{
    SecretKey symmetricKey_write;
    SecretKey symmetricKey_read;
    int idx_write;
    int idx_read;
    byte[] tag_write;
    byte[] tag_read;
    byte[] iv_write;
    byte[] iv_read;
    byte[] salt_write;
    byte[] salt_read;
    String name;
    List<String> messageHys = new ArrayList<>();

    public DataFriend(int idx_read, int idx_write, byte[] tag_read, byte[] tag_write, String name, SecretKey symmetricKey_read,
                      SecretKey symmetricKey_write, byte[] iv_read, byte[] iv_write, byte[] salt_read, byte[] salt_write){
        this.idx_read = idx_read;
        this.idx_write = idx_write;
        this.tag_read = tag_read;
        this.tag_write = tag_write;
        this.name = name;
        this.symmetricKey_read = symmetricKey_read;
        this.symmetricKey_write = symmetricKey_write;
        this.iv_read = iv_read;
        this.iv_write = iv_write;
        this.salt_write = salt_write;
        this.salt_read = salt_read;
    }

    public void addMessage(String message) {
        messageHys.add(message);
    }
    public List<String> getMessageHys() {
        return messageHys;
    }
}