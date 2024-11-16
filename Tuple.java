public class Tuple {
    private String key;
    private Message value;

    public Tuple(String key, Message value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Message getValue() {
        return value;
    }
}