package ws;

public class DeviceFrame {

    public static final byte HEADER = (byte) 0xAB;

    private final byte[] frame;
    private final boolean hasBattery;


    public DeviceFrame(
            int deviceId,           // uint16
            short temperature,      // int16
            //boolean fanOn,          // uint8
           // boolean heaterOn,       // uint8

            int fanState,
            int heaterState,

            Integer batteryLevel,   // if null = yok
           int status               //bitmask
    ) {
        hasBattery = (batteryLevel != null);


        int dataLength = hasBattery ? 8 : 7;
        frame = new byte[dataLength + 2];   // +2 CRC

        frame[0] = HEADER;

        // Device ID (uint16, little-endian)
        frame[1] = (byte) deviceId;
        frame[2] = (byte) (deviceId >> 8);      //right shift operator, Move all bits 8 places to the right. same as division by 256

        // Temperature (int16, little-endian)
        frame[3] = (byte) temperature;
        frame[4] = (byte) (temperature >> 8);

        // Fan/heater state
        frame[5] = (byte) (fanOn ? 1 : 0);      //-> ternary operator if on 1, off 0
        frame[6] = (byte) (heaterOn ? 1 : 0);

        int statusIndex;
        if (hasBattery) {
            frame[7] = (byte) batteryLevel.intValue();
            statusIndex = 8;
        } else {
            statusIndex = 7;
        }
        frame[statusIndex] = (byte) status;



        //CRC-16 IBM-3740
        int crc = CRC16IBM3740.compute(frame, dataLength); // CRC over data bytes only

        frame[dataLength] = (byte) (crc >> 8);     // MSB most significant byte largest val
        frame[dataLength + 1] = (byte) crc;            // LSB least significant smallest val
    }



    // ---- Constructor (parse received frame) ----
    public DeviceFrame(byte[] received) {
        if (received.length != 10 && received.length != 11) {
            throw new IllegalArgumentException("Invalid frame length");
        }

        frame = received.clone();
        hasBattery = (frame.length == 11);

        if (frame[0] != HEADER) {
            throw new IllegalArgumentException("Invalid header");
        }

        if (!isChecksumValid()) {
            throw new IllegalArgumentException("Invalid CRC");
        }

        if ((getStatus() & 0b1110_0000) != 0) {
            throw new IllegalArgumentException("Invalid status bits set");
        }
    }

    // Getters, decode fields
    public int getDeviceId() {
        return ((frame[2] & 0xFF) << 8) | (frame[1] & 0xFF);
    }


    //java float16 supportlamadığı için manuel olarak convertion
    public float getTemperature() {
        int half = ((frame[4] & 0xFF) << 8) | (frame[3] & 0xFF);
        return halfToFloat(half);
    }

    public boolean isFanOn() {
        return frame[5] == 1;
    }

    public boolean isHeaterOn() {
        return frame[6] == 1;

    }

    public boolean hasBattery() {
        return hasBattery;
    }

    public int getBatteryLevel() {
        return hasBattery() ? (frame[7] & 0xFF) : -1;

    }

    public int getStatus() {
        int statusIndex = hasBattery() ? 8 : 7;
        return frame[statusIndex] & 0xFF;
    }


    // CRC validation
    private boolean isChecksumValid() {
        int dataLength = frame.length - 2;

        int receivedCrc =
                ((frame[dataLength] & 0xFF) << 8) |
                        (frame[dataLength + 1] & 0xFF);

        int calculatedCrc = CRC16IBM3740.compute(frame, dataLength);

        return receivedCrc == calculatedCrc;
    }



    public String toHexString() {
        StringBuilder sb = new StringBuilder();
        for (byte b : frame) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }


    //temp--> float float16 conversion
    private static float halfToFloat(int half) {

        int sign = (half >> 15) & 0x1;
        int exp = (half >> 10) & 0x1F;
        int mant = half & 0x3FF;

        if (exp == 0) {
            // subnormal
            return (float) ((sign == 1 ? -1 : 1)
                    * Math.pow(2, -14)
                    * (mant / 1024.0));
        }
        if (exp == 31) {
            return mant == 0
                    ? (sign == 1 ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY)
                    : Float.NaN;
        }

        return (float) ((sign == 1 ? -1 : 1)
                * Math.pow(2, exp - 15)
                * (1 + mant / 1024.0));
    }
}

