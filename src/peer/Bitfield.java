package peer;

public class Bitfield {
    private byte[] bitfield;

    public Bitfield(int size) {
        bitfield = new byte[size];
    }

    public void setAllBitsToOne() {
        for (int i = 0; i < bitfield.length; i++) {
            bitfield[i] = (byte) 0xFF; // Set all bits to 1
        }
    }

    public void setAllBitsToZero() {
        for (int i = 0; i < bitfield.length; i++) {
            bitfield[i] = (byte) 0x00; // Set all bits to 0
        }
    }

    public byte[] getBitfield() {
        return bitfield;
    }

    public void updateSize(int newSize) {
        byte[] newBitfield = new byte[newSize];
        System.arraycopy(bitfield, 0, newBitfield, 0, Math.min(bitfield.length, newBitfield.length));
        bitfield = newBitfield;
    }
}
