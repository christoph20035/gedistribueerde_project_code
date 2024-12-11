import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
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
    byte[] salt;
    String name;
    byte[] hashed_state_write;
    byte[] hashed_state_read;
    List<String> messageHys = new ArrayList<>();

    MessageDigest digest = MessageDigest.getInstance("SHA-256");

    public DataFriend(int idx_read, int idx_write, byte[] tag_read, byte[] tag_write, String name, SecretKey symmetricKey_read,
                      SecretKey symmetricKey_write, byte[] iv_read, byte[] iv_write, byte[] salt) throws NoSuchAlgorithmException, IOException {
        this.idx_read = idx_read;
        this.idx_write = idx_write;
        this.tag_read = tag_read;
        this.tag_write = tag_write;
        this.name = name;
        this.symmetricKey_read = symmetricKey_read;
        this.symmetricKey_write = symmetricKey_write;
        this.iv_read = iv_read;
        this.iv_write = iv_write;
        this.salt = salt;
        this.hashed_state_write = setHashedState(idx_write, tag_write, symmetricKey_write);
        this.hashed_state_read = setHashedState(idx_read, tag_read, symmetricKey_read);
    }

    public void addMessage(String message) {
        messageHys.add(message);
    }
    public List<String> getMessageHys() {
        return messageHys;
    }

    public void clearMessageHys(){
        messageHys.clear();
    }

    public void induceCorrupted(){
        idx_write++;
        //symmetricKey_read = symmetricKey_write;
    }

    public byte[] setHashedState(int idx, byte[] tag, SecretKey symmetricKey) throws IOException {
        byte[] keyBytes = symmetricKey.getEncoded();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(ByteBuffer.allocate(4).putInt(idx).array());
        outputStream.write(tag);
        outputStream.write(keyBytes);
        byte[] state = outputStream.toByteArray();
        return digest.digest(state);
    }
}