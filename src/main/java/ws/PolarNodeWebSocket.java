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
        view.connect.setEnabled(false);

        HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create("wss://polarnode.alsoft.nl"), new WebSocketListener())
                .thenAccept(ws -> {
                    this.socket = ws;
                    SwingUtilities.invokeLater(() -> {
                        view.disconnect.setEnabled(true);
                        view.showWarning("Connected");
                    });
                })
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        view.connect.setEnabled(true);
                        view.showWarning("Failed to Connect");
                    });
                    return null;
                });
    }

    public void disconnect() {
        if (socket != null) {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, "User Exit")
                    .thenRun(() -> {
                        System.out.println("Closing application...");
                        System.exit(0);
                    });
        } else {
            System.exit(0);
        }
    }


    public void setFan(int lvl) {
        send(CommandFrame.CMD_SET_FAN, lvl);
    }

    public void setHeater(int lvl) {
        send(CommandFrame.CMD_SET_HEATER, lvl);
    }

    private void send(byte cmd, int lvl) {
        if (socket == null) return;

        CommandFrame frame = new CommandFrame(cmd, lvl);
        socket.sendBinary(ByteBuffer.wrap(frame.toByteArray()), true);

        System.out.println(" Command sent: " + (cmd == CommandFrame.CMD_SET_FAN ? "FAN" : "HEATER") + " = " + lvl);

    }


    private class WebSocketListener implements Listener {

        @Override
        public void onOpen(WebSocket ws) {
            ws.request(1);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data,  boolean last) {

            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);

            try {
                DeviceFrame frame = new DeviceFrame(bytes);
                SwingUtilities.invokeLater(() -> {
                    view.updateTelemetry(frame);
                });

                fanHeaterController.regulate(frame);

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> view.showWarning("Invalid Data Frame"));            }

            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("WebSocket Error: " + error.getMessage());
            disconnect();
        }

        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("Server closed connection: " + reason);
            disconnect();
            return null;
        }
    }
}

