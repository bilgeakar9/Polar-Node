package ws;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Instant;



public class View extends JFrame {
    private final Color BG_COLOR = new Color(28, 28, 33);
    private final Color CARD_COLOR = new Color(43, 43, 48);
    private final Color ACCENT_COLOR = new Color(0, 255, 150);

    public JButton connect = createStyledButton("Connect", new Color(46, 204, 113));
    public JButton disconnect = createStyledButton("Disconnect", new Color(231, 76, 60));

    private JLabel tempLabel = createDataLabel("0.0");
    private JLabel fanLabel = createDataLabel("OFF");
    private JLabel heaterLabel = createDataLabel("OFF");
    private JLabel batteryLabel = createDataLabel("0%");
    public JLabel warningLabel = new JLabel("Status: Offline");

    private GraphPanel graphPanel = new GraphPanel();

    public View() {
        setTitle("Polar Node");
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(15, 15));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Sidebar Stats
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_COLOR);
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 10));
        sidebar.add(createStatBox("Temperature (°C)", tempLabel));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createStatBox("Fan", fanLabel));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createStatBox("Heater", heaterLabel));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createStatBox("Battery", batteryLabel));

        // Bottom Bar (Warnings + Buttons)
        JPanel bottomArea = new JPanel(new BorderLayout());
        bottomArea.setBackground(BG_COLOR);
        bottomArea.setBorder(new EmptyBorder(0, 10, 10, 10));

        warningLabel.setForeground(new Color(150, 150, 150));
        warningLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.add(connect);
        buttonPanel.add(disconnect);
        disconnect.setEnabled(false);

        bottomArea.add(warningLabel, BorderLayout.WEST);
        bottomArea.add(buttonPanel, BorderLayout.EAST);

        add(sidebar, BorderLayout.WEST);
        add(graphPanel, BorderLayout.CENTER);
        add(bottomArea, BorderLayout.SOUTH);

        setSize(1000, 650);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updateTelemetry(DeviceFrame frame) {
        tempLabel.setText(String.format("%.1f", frame.getTemperature()));
        batteryLabel.setText(frame.getBatteryLevel() + "%");
        fanLabel.setText(formatLevel(frame.getFanLevel()));
        heaterLabel.setText(formatLevel(frame.getHeaterLevel()));


        graphPanel.addValue(frame.getTemperature());


        StringBuilder statusMsg = new StringBuilder("Status: ");
        int status = frame.getStatus();
        boolean isCritical = false;


        //status bitmask
        if ((status & 0x01) != 0 || frame.getBatteryLevel() < 10) {
            statusMsg.append("!! LOW POWER !! | ");
            isCritical = true;
        }
        if ((status & 0x02) != 0) {
            statusMsg.append("!! TOO COLD !! | ");
            isCritical = true;
        }
        if ((status & 0x04) != 0) {
            statusMsg.append("!! TOO HOT !! | ");
            isCritical = true;
        }
        if ((status & 0x08) != 0) {
            statusMsg.append("!! CONTROL ERROR !! | ");
            isCritical = true;
        }
        //if ((status & 0x10) != 0) {
          //  statusMsg.append("MAINTENANCE REQUIRED. | ");
        //}


        if (frame.getFanLevel() > 0) {
            statusMsg.append("FAN: ").append(formatLevel(frame.getFanLevel())).append(" | ");
        }
        if (frame.getHeaterLevel() > 0) {
            statusMsg.append("HEATER: ").append(formatLevel(frame.getHeaterLevel())).append(" | ");
        }


        if (statusMsg.length() <= 8) {
            statusMsg.append(" ");
            warningLabel.setText(statusMsg.toString());
            warningLabel.setForeground(new Color(150, 150, 150)); // Gray
        } else {
            // Remove trailing " | "
            String finalMsg = statusMsg.toString().replaceAll(" \\| $", "");
            warningLabel.setText(finalMsg);
            warningLabel.setForeground(isCritical ? Color.RED : new Color(255, 165, 0)); // Red or Orange
        }
        logToCSV(frame);
    }

    private String formatLevel(int lvl) {
        if (lvl >= 2) return "HIGH";
        if (lvl == 1) return "LOW";
        return "OFF";
    }

    public void showWarning(String msg) {
        warningLabel.setText("Status: " + msg);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(130, 40));
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        return b;
    }

    private JPanel createStatBox(String title, JLabel val) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_COLOR);
        p.setBorder(new EmptyBorder(10, 15, 10, 15));
        p.setMaximumSize(new Dimension(200, 80));
        JLabel t = new JLabel(title.toUpperCase());
        t.setFont(new Font("SansSerif", Font.BOLD, 10));
        t.setForeground(Color.LIGHT_GRAY);
        p.add(t, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JLabel createDataLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.BOLD, 24));
        l.setForeground(ACCENT_COLOR);
        return l;
    }


    class GraphPanel extends JPanel {
        private List<Double> points = new ArrayList<>();
        private final int MAX_POINTS = 60;
        private final int MARGIN = 40;

        public void addValue(double v) {
            points.add(v);
            if (points.size() > MAX_POINTS) points.remove(0);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(35, 35, 40));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw Y-Axis (0 to 10)
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            int minTemp = -4;
            int maxTemp = 14;
            int range = maxTemp - minTemp;

            for (int i = minTemp; i <= maxTemp; i += 2) {
                int y = getHeight() - ((i - minTemp) * (getHeight() - 80) / range) - 40;

                g2.drawString(String.valueOf(i), 10, y + 5);
                g2.setColor(new Color(255, 255, 255, 20));


                g2.drawLine(MARGIN, y, getWidth(), y);
                g2.setColor(Color.DARK_GRAY);
            }

            if (points.size() < 2) return;


            g2.setColor(new Color(255, 69, 58));
            g2.setStroke(new BasicStroke(3f));

            for (int i = 0; i < points.size() - 1; i++) {
                int x1 = MARGIN + (i * (getWidth() - MARGIN) / (MAX_POINTS - 1));
                int x2 = MARGIN + ((i + 1) * (getWidth() - MARGIN) / (MAX_POINTS - 1));


                int y1 = getHeight() - (int) ((points.get(i) - minTemp) * (getHeight() - 80) / (double)range) - 40;
                int y2 = getHeight() - (int) ((points.get(i + 1) - minTemp) * (getHeight() - 80) / (double)range) - 40;

                g2.drawLine(x1, y1, x2, y2);
            }
        }
    }

    public void logToCSV(DeviceFrame frame) {
        try (PrintWriter out = new PrintWriter(new FileWriter("polarnode_log.csv", true))) {
            out.printf("%s,%d,%.2f,%d,%d,%d\n",
                    Instant.now().toString(),
                    frame.getDeviceId(),
                    frame.getTemperature(),
                    frame.getFanLevel(),
                    frame.getHeaterLevel(),
                    frame.getBatteryLevel()
            );
        } catch (Exception e) {
            System.err.println("Logging failed: " + e.getMessage());
        }
    }
}