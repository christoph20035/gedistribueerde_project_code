import javax.crypto.SecretKey;

public class DataFriend{
    SecretKey symmetricKey_write;
    SecretKey symmetricKey_read;
    int idx_write;
    int idx_read;
    byte[] tag_write;
    byte[] tag_read;
    String name;

    public DataFriend(int idx_read, int idx_write, byte[] tag_read, byte[] tag_write, String name, SecretKey symmetricKey_read, SecretKey symmetricKey_write){
        this.idx_read = idx_read;
        this.idx_write = idx_write;
        this.tag_read = tag_read;
        this.tag_write = tag_write;
        this.name = name;
        this.symmetricKey_read = symmetricKey_read;
        this.symmetricKey_write = symmetricKey_write;
    }

}