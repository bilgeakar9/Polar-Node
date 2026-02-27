package ws;

public class Main {
    public static void main(String[] args) {
        System.setProperty("apple.awt.UIElement", "false");
        View view = new View();
        view.setVisible(true);

        PolarNodeWebSocket socket= new PolarNodeWebSocket(view);
        socket.connect();

    }
}
