package ws;

public class CommandFrame {

    public static final byte HEADER = (byte) 0xBA;

    public static final byte CMD_SET_FAN = 0x01;
    public static final byte CMD_SET_HEATER = 0x02;

    private final byte[] frame = new byte[5];

    public CommandFrame(byte command, boolean on) {
        frame[0] = HEADER;
        frame[1] = command;
        frame[2] = (byte) (on ? 1 : 0);

        int crc = CRC16IBM3740.compute(frame, 3);
        frame[3] = (byte) (crc >> 8);
        frame[4] = (byte) crc;
    }

    public byte[] toByteArray() {
        return frame;
    }

//    //hex to string
//    public String toHexString() {
//        StringBuilder sb = new StringBuilder();
//        for (byte b : frame) {
//            sb.append(String.format("%02X ", b & 0xFF));
//        }
//        return sb.toString().trim();
//    }
}