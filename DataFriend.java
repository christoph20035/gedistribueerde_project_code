import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataFriend {
    @Expose
    @SerializedName("symmetricKey_writeString")
    String symmetricKey_writeString;
    @Expose
    @SerializedName("symmetricKey_readString")
    String symmetricKey_readString;
    @Expose
    @SerializedName("tag_writeString")
    String tag_writeString;
    @Expose
    @SerializedName("tag_readString")
    String tag_readString;
    @Expose
    @SerializedName("iv_writeString")
    String iv_writeString;
    @Expose
    @SerializedName("iv_readString")
    String iv_readString;
    @Expose
    @SerializedName("saltString")
    String saltString; // Keep as String in JSON

    SecretKey symmetricKey_write;
    SecretKey symmetricKey_read;
    @Expose
    int idx_write;
    @Expose
    int idx_read;
    byte[] tag_write;
    byte[] tag_read;
    byte[] iv_write;
    byte[] iv_read;
    byte[] salt; // Exclude from direct Gson deserialization
    @Expose
    String name;
    byte[] hashed_state_write;
    byte[] hashed_state_read;
    List<String> messageHys = new ArrayList<>();
    transient MessageDigest digest; // Exclude from serialization

    public void initialize() throws NoSuchAlgorithmException, IOException {
        // Decode Base64 fields
        byte[] decoded_symmetricKey_read = Base64.getDecoder().decode(symmetricKey_readString);
        byte[] decoded_symmetricKey_write = Base64.getDecoder().decode(symmetricKey_writeString);
        this.tag_read = Base64.getDecoder().decode(tag_readString);
        this.tag_write = Base64.getDecoder().decode(tag_writeString);
        this.iv_read = Base64.getDecoder().decode(iv_readString);
        this.iv_write = Base64.getDecoder().decode(iv_writeString);
        this.salt = Base64.getDecoder().decode(saltString); // Decode saltString

        // Create SecretKey objects
        this.symmetricKey_read = new SecretKeySpec(decoded_symmetricKey_read, 0, decoded_symmetricKey_read.length, "AES");
        this.symmetricKey_write = new SecretKeySpec(decoded_symmetricKey_write, 0, decoded_symmetricKey_write.length, "AES");

        // Initialize MessageDigest
        this.digest = MessageDigest.getInstance("SHA-256");

        // Compute hashed states
        this.hashed_state_write = setHashedState(this.idx_write, this.tag_write, this.symmetricKey_write);
        this.hashed_state_read = setHashedState(this.idx_read, this.tag_read, this.symmetricKey_read);
        messageHys = new ArrayList<>();
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