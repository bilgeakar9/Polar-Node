package ws;

public class FanHeaterController {

    private final PolarNodeWebSocket socket;

    private static final float TOO_COLD = 0.0f;
    private static final float TOO_HOT = 10.0f;


    private static final float HEATER_OFF_TEMP = 1.0f;
    private static final float FAN_OFF_TEMP = 9.0f;

    private static final int OFF_STATE = 0;
    private static final int LOW_STATE = 1;
    private static final int HIGH_STATE = 2;



    public FanHeaterController(PolarNodeWebSocket socket) {
        this.socket = socket;
    }
    public void regulate(DeviceFrame frame) {

        float temp = frame.getTemperature();
        boolean fanOn = frame.isFanOn();
        boolean heaterOn = frame.isHeaterOn();
        int battery = frame.getBatteryLevel();


        if (battery >= 0 && battery < 10) {
            System.out.println("POWER LOW");
        }


        if (fanOn && heaterOn) {
            System.out.println("CONTROL ERROR ");
            return;
        }


        if (temp < TOO_COLD) {
            socket.setHeater(true);
            socket.setFan(false);
            return;
        }


        if (temp > TOO_HOT) {
            socket.setFan(true);
            socket.setHeater(false);
            return;
        }


        if (heaterOn && temp > HEATER_OFF_TEMP) {
            socket.setHeater(false);
        }

        if (fanOn && temp < FAN_OFF_TEMP) {
            socket.setFan(false);
        }
    }
}
