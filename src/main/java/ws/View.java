package ws;

import javax.swing.*;
import java.awt.*;


public class View extends JFrame {
    public JButton connect = new JButton("Connect");
    public JButton disconnect = new JButton("Disconnect");

    public JLabel temperature = new JLabel("Temperature");
    public JLabel fan = new JLabel("Fan");
    public JLabel heater = new JLabel("Heater");
    public JLabel battery= new JLabel("Battery");
    public JLabel warning = new JLabel("Warning");

    public JTextField temperatureField = new JTextField("");
    public JTextField fanField = new JTextField("");
    public JTextField heaterField = new JTextField("");
    public JTextField batteryField = new JTextField("");
    public JTextField warningField = new JTextField("");




    public View(){
        setTitle("Polar Node");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));


        JPanel telemetryPanel = new JPanel(new GridLayout(5, 2, 8, 8));

        telemetryPanel.add(new JLabel("Temperature:"));
        telemetryPanel.add(temperatureField);

        telemetryPanel.add(new JLabel("Fan:"));
        telemetryPanel.add(fanField);

        telemetryPanel.add(new JLabel("Heater:"));
        telemetryPanel.add(heaterField);

        telemetryPanel.add(new JLabel("Battery:"));
        telemetryPanel.add(batteryField);

        telemetryPanel.add(new JLabel("Warning:"));
        telemetryPanel.add(warningField);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(connect);
        buttonPanel.add(disconnect);


        JTextField[] fields = {
                temperatureField, fanField, heaterField, batteryField, warningField
        };

        for (JTextField f : fields) {
            f.setEditable(false);
            f.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }

        add(telemetryPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

    }
    public void updateTelemetry(DeviceFrame frame) {
        SwingUtilities.invokeLater(() -> {
            temperatureField.setText(String.format("%.1f", frame.getTemperature()));

            fanField.setText(levelToString(frame.getFanLevel()));
            heaterField.setText(levelToString(frame.getHeaterLevel()));

            batteryField.setText(frame.getBatteryLevel() + "%");


            String warning = "None";

            if (frame.getHeaterLevel() > 0 && frame.getFanLevel() > 0) {
                warning = "Control Error: Fan & Heater ON";
            }
            else if (frame.getFanLevel() > 0) {
                warning = "Fan: " + frame.getFanLevel();
            }
            else if (frame.getHeaterLevel() > 0) {
                warning = "Heater: " + frame.getHeaterLevel();
            }

            warningField.setText(warning);
            warningField.setForeground(
                    warning.equals("None") ? Color.BLACK : Color.BLUE
            );

            });
    }


    private String levelToString(int level) {
        switch (level) {
            case 0: return "OFF";
            case 1: return "LOW";
            case 2: return "HIGH";
            default: return "UNKNOWN";
        }
    }


    public void showWarning(String message) {
        SwingUtilities.invokeLater(() -> {
            warningField.setText(message);
            warningField.setForeground(Color.GRAY);
        });
    }
}
