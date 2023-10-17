package src.peer;

public enum MessageType {
    BITFIELD(0),
    CHOKE(1),
    HANDSHAKE(2),
    INTERESTED(3),
    PIECE(4),
    REQUEST(5),
    UNCHOKE(6),
    UNINTERESTED(7);

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
