package ws;

public class FanHeaterController {

    private final PolarNodeWebSocket socket;

    private static final float TOO_COLD = 0.0f;
    private static final float TOO_HOT = 10.0f;


    private static final float HEATER_OFF_TEMP = 1.0f;
    private static final float FAN_OFF_TEMP = 9.0f;

    private static final int LOW_BATTERY = 10;


    public FanHeaterController(PolarNodeWebSocket socket) {
        this.socket = socket;
    }

    public void regulate(DeviceFrame frame) {

        float temp = frame.getTemperature();
        int fanLevel = frame.getFanLevel();
        int heaterlevel = frame.getHeaterLevel();
        int battery = frame.getBatteryLevel();


        if (battery >= 0 && battery < LOW_BATTERY) {
            System.out.println("POWER LOW");
        }


        if (temp < TOO_COLD) {
            frame.setHeaterLevel(2);
            frame.setFanLevel(0);
            socket.setFan(0);
            socket.setHeater(2);
            return;
        }


        if (temp > TOO_HOT) {
            frame.setFanLevel(2);
            frame.setHeaterLevel(0);
            socket.setFan(2);
            socket.setHeater(0);
            return;
        }

        if (temp < HEATER_OFF_TEMP +1 ) {
            // Turn on heater low/high, keep fan off
            frame.setHeaterLevel(1);
            frame.setFanLevel(0);
            socket.setHeater(1);
            socket.setFan(0);
        } else if (temp > FAN_OFF_TEMP -1) {
            // Turn on fan low/high, keep heater off
            frame.setFanLevel(1);
            frame.setHeaterLevel(0);
            socket.setFan(1);
            socket.setHeater(0);
        } else {
            // Comfortable range - both off
            frame.setHeaterLevel(0);
            frame.setFanLevel(0);
            socket.setHeater(0);
            socket.setFan(0);
        }
    }
}
