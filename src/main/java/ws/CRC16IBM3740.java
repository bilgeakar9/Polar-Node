package ws;

public class CRC16IBM3740 {
    public static int compute(byte[] data, int length) {
        int crc = 0xFFFF;
        int poly = 0x1021;

        for (int i = 0; i < length; i++) {
            crc ^= (data[i] & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ poly;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF;
            }
        }
        return crc;
    }
}
