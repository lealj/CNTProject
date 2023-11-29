package message;

public enum MessageType {

    CHOKE(0),
    UNCHOKE(1),
    INTERESTED(2),
    UNINTERESTED(3),
    HAVE(4),
    BITFIELD(5),
    REQUEST(6),
    PIECE(7);

    private final int val;

    MessageType(int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }

    public static MessageType fromValue(int val) {

        for (MessageType type : values()) {

            if (type.getValue() == val) {
                return type;
            }
        }

        return null;
    }
}
