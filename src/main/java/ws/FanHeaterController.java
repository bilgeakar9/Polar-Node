package ws;

public class FanHeaterController {

    private final PolarNodeWebSocket socket;

    private static final float TOO_COLD = 0.0f;
    private static final float TOO_HOT = 10.0f;

    private static final float HEATER_LOW = 3.0f;
    private static final float HEATER_HIGH = 2.0f;
    private static final float HEATER_STOP = 4.0f;

    private static final float FAN_LOW = 7.5f;
    private static final float FAN_HIGH = 8.5f;
    private static final float FAN_STOP = 6.5f;


    private static final int LOW_BATTERY = 10;


    private int currentFanLevel = 0;
    private int currentHeaterLevel = 0;


    public FanHeaterController(PolarNodeWebSocket socket) {
        this.socket = socket;
    }

    public void regulate(DeviceFrame frame) {

        float temp = frame.getTemperature();
        int battery = frame.getBatteryLevel();

        int fanLevel = currentFanLevel;
        int heaterLevel = currentHeaterLevel;

        if (battery <= 0) {
            currentFanLevel = 0;
            currentHeaterLevel = 0;
            frame.setWarning("BATTERY=0 - SHUTDOWN");

            socket.setFan(0);
            socket.setHeater(0);

            frame.setFanLevel(0);
            frame.setHeaterLevel(0);
            return;
        }

        if (battery < LOW_BATTERY) {
            frame.setWarning("Low Battery/Power Mode");
        }


        if (temp <= HEATER_HIGH) {
            heaterLevel = (battery < LOW_BATTERY) ? 1 : 2;
            fanLevel = 0;
        }
        else if (temp <= HEATER_LOW) {
            heaterLevel = 1;
            fanLevel = 0;
        }
        else if (temp >= HEATER_STOP) {
            heaterLevel = 0;
        }


        if (temp >= FAN_HIGH) {
            fanLevel = (battery < LOW_BATTERY) ? 1 : 2;
            heaterLevel = 0;
        }
        else if (temp >= FAN_LOW) {
            fanLevel = 1;
            heaterLevel = 0;
        }
        else if (temp <= FAN_STOP) {
            fanLevel = 0;
        }


        if (temp <= TOO_COLD) {
            heaterLevel = (battery < LOW_BATTERY) ? 1 : 2;
            fanLevel = 0;

        }

        if (temp >= TOO_HOT) {
            fanLevel = (battery < LOW_BATTERY) ? 1 : 2;
            heaterLevel = 0;
        }


        currentFanLevel = fanLevel;
        currentHeaterLevel = heaterLevel;

        frame.setFanLevel(fanLevel);
        frame.setHeaterLevel(heaterLevel);


        socket.setFan(fanLevel);
        socket.setHeater(heaterLevel);
    }

}
