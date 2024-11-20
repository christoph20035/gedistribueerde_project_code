import java.util.Arrays;
import java.util.Objects;

public class Tuple {
    private byte[] tag;
    private byte[] data;

    public Tuple(byte[] data, byte[] tag) {
        this.tag = tag;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple tuple = (Tuple) o;
        return tag == tuple.tag && data == tuple.data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(tag), Arrays.hashCode(data));
    }

    public byte[] getTag(){
        return tag;
    }
    public byte[] getData(){
        return data;
    }

}