package ws;

import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

public class PolarNodeWebSocket {
    private WebSocket socket;
    private final View view;
    private final FanHeaterController fanHeaterController;


    public PolarNodeWebSocket(View view) {
        this.view = view;
        this.fanHeaterController = new FanHeaterController(this);

        view.connect.addActionListener(e -> connect());
        view.disconnect.addActionListener(e -> disconnect());
    }



    public void connect() {
        String uri = "wss://polarnode.alsoft.nl";    //ibm-3740

        HttpClient client = HttpClient.newHttpClient();

        socket = client.newWebSocketBuilder().buildAsync(URI.create(uri), new WebSocketListener()).join();

        System.out.println("Connected to WebSocket");

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (socket != null) {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, "User disconnect");
            socket = null;
        }
    }



    public void setFan(boolean on) {
        send(CommandFrame.CMD_SET_FAN, on);
    }

    public void setHeater(boolean on) {
        send(CommandFrame.CMD_SET_HEATER, on);
    }

    private void send(byte cmd, boolean on) {
        if (socket == null) return;

        CommandFrame frame = new CommandFrame(cmd, on);
        socket.sendBinary(ByteBuffer.wrap(frame.toByteArray()), true);

        System.out.println(" Command sent: " + (cmd == CommandFrame.CMD_SET_FAN ? "FAN" : "HEATER") + " = " + on);

    }


    private class WebSocketListener implements Listener {

        @Override
        public void onOpen(WebSocket ws) {
            view.showWarning("WebSocket opened");
            ws.request(1);
        }

        @Override
        public CompletionStage<?> onBinary(
                WebSocket webSocket,
                ByteBuffer data,
                boolean last
        ) {

            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);

            try {

                DeviceFrame frame = new DeviceFrame(bytes);
                SwingUtilities.invokeLater(() -> {
                    view.updateTelemetry(frame);
                });

                fanHeaterController.regulate(frame);

            } catch (Exception e) {
               view.showWarning("Invalid telemetry frame received");
            }

            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            error.printStackTrace();
        }
    }
}

