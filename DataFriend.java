import javax.crypto.SecretKey;

public class DataFriend{
    SecretKey sendSymmetricKey;
    SecretKey receiveSymmetricKey;
    byte[] idx;
    byte[] tag;
    String name;

    public DataFriend(byte[] idx, byte[] tag, String name, SecretKey sendSymmetricKey, SecretKey receiveSymmetricKey){
        this.idx = idx;
        this.tag = tag;
        this.name = name;
        this.sendSymmetricKey = sendSymmetricKey;
        this.receiveSymmetricKey = receiveSymmetricKey;
    }

    public DataFriend(String name){
        this.name = name;
    }
}