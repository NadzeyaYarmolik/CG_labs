// ColorPickerApp.java
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class ColorPickerApp extends JFrame {
    private int r = 0, g = 0, b = 0;
    private double currentH = 0.0;
    private double currentS = 0.0;
    private double currentV = 100.0;
    private boolean isUpdating = false;

    private JPanel previewPanel;
    private JLabel svCanvas;
    private JLabel hueCanvas;
    private JSlider[] sliders = new JSlider[10];
    private JTextField[] fields = new JTextField[10];

    private BufferedImage svImage;
    private BufferedImage hueImage;

    public ColorPickerApp() {
        setTitle("Lab 1 - Color Picker (Java Swing)");
        setSize(780, 530);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
        updateAllFromRgb(0, 0, 0);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Левая панель: графический квадрат S/V и спектр H (никаких наложений)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        svImage = new BufferedImage(220, 220, BufferedImage.TYPE_INT_RGB);
        svCanvas = new JLabel(new ImageIcon(svImage));
        svCanvas.setPreferredSize(new Dimension(220, 220));
        svCanvas.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        svCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleSvClick(e.getX(), e.getY()); }
        });
        svCanvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) { handleSvClick(e.getX(), e.getY()); }
        });

        hueImage = new BufferedImage(220, 25, BufferedImage.TYPE_INT_RGB);
        drawHueBar();
        hueCanvas = new JLabel(new ImageIcon(hueImage));
        hueCanvas.setPreferredSize(new Dimension(220, 25));
        hueCanvas.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        hueCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleHueClick(e.getX()); }
        });
        hueCanvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) { handleHueClick(e.getX()); }
        });

        leftPanel.add(svCanvas);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(hueCanvas);
        add(leftPanel, BorderLayout.WEST);

        // Правая панель: отдельно плашка превью цвета сверху, ниже — ползунки
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        previewPanel = new JPanel();
        previewPanel.setPreferredSize(new Dimension(420, 45));
        previewPanel.setMaximumSize(new Dimension(420, 45));
        previewPanel.setBackground(Color.BLACK);
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JPanel previewWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        previewWrapper.add(new JLabel("Текущий цвет: "));
        previewWrapper.add(previewPanel);
        
        rightPanel.add(previewWrapper);
        rightPanel.add(Box.createVerticalStrut(10));

        addGroup(rightPanel, "HSV Модель", new String[]{"H", "S", "V"}, new int[]{360, 100, 100}, new int[]{0, 1, 2});
        addGroup(rightPanel, "RGB Модель", new String[]{"R", "G", "B"}, new int[]{255, 255, 255}, new int[]{3, 4, 5});
        addGroup(rightPanel, "CMYK Модель", new String[]{"C", "M", "Y", "K"}, new int[]{100, 100, 100, 100}, new int[]{6, 7, 8, 9});

        add(rightPanel, BorderLayout.CENTER);
    }

    private void addGroup(JPanel parent, String title, String[] names, int[] maxVals, int[] indices) {
        JPanel group = new JPanel();
        group.setBorder(BorderFactory.createTitledBorder(title));
        group.setLayout(new GridLayout(names.length, 3, 5, 2));
        group.setMaximumSize(new Dimension(420, 115));

        for (int i = 0; i < names.length; i++) {
            int idx = indices[i];
            group.add(new JLabel(names[i] + ":"));

            JTextField tf = new JTextField("0", 4);
            fields[idx] = tf;
            group.add(tf);

            JSlider slider = new JSlider(0, maxVals[i], 0);
            sliders[idx] = slider;
            slider.addChangeListener(e -> {
                if (isUpdating) return;
                onSliderChange(idx);
            });
            group.add(slider);
        }
        parent.add(group);
    }

    private void drawHueBar() {
        for (int x = 0; x < 220; x++) {
            double h = (x / 220.0) * 360.0;
            int rgb = Color.HSBtoRGB((float)(h / 360.0), 1.0f, 1.0f);
            for (int y = 0; y < 25; y++) {
                hueImage.setRGB(x, y, rgb);
            }
        }
    }

    private void drawSvSquare() {
        for (int y = 0; y < 220; y++) {
            float v = 1.0f - (y / 220.0f);
            for (int x = 0; x < 220; x++) {
                float s = x / 220.0f;
                int rgb = Color.HSBtoRGB((float)(currentH / 360.0), s, v);
                svImage.setRGB(x, y, rgb);
            }
        }
        svCanvas.repaint();
    }

    private void handleHueClick(int x) {
        x = Math.max(0, Math.min(219, x));
        currentH = (x / 220.0) * 360.0;
        int[] rgb = Converter.hsvToRgb(currentH, currentS, currentV);
        updateAllFromRgb(rgb[0], rgb[1], rgb[2]);
    }

    private void handleSvClick(int x, int y) {
        x = Math.max(0, Math.min(219, x));
        y = Math.max(0, Math.min(219, y));
        currentS = (x / 220.0) * 100.0;
        currentV = (1.0 - (y / 220.0)) * 100.0;
        int[] rgb = Converter.hsvToRgb(currentH, currentS, currentV);
        updateAllFromRgb(rgb[0], rgb[1], rgb[2]);
    }

    private void onSliderChange(int idx) {
        isUpdating = true;
        int val = sliders[idx].getValue();
        fields[idx].setText(String.valueOf(val));

        if (idx >= 0 && idx <= 2) {
            if (idx == 0) currentH = val;
            if (idx == 1) currentS = val;
            if (idx == 2) currentV = val;
            int[] rgb = Converter.hsvToRgb(currentH, currentS, currentV);
            r = rgb[0]; g = rgb[1]; b = rgb[2];
        } else if (idx >= 3 && idx <= 5) {
            r = sliders[3].getValue();
            g = sliders[4].getValue();
            b = sliders[5].getValue();
            double[] hsv = Converter.rgbToHsv(r, g, b, currentH, currentS);
            currentH = hsv[0]; currentS = hsv[1]; currentV = hsv[2];
        } else if (idx >= 6 && idx <= 9) {
            double c = sliders[6].getValue();
            double m = sliders[7].getValue();
            double y = sliders[8].getValue();
            double k = sliders[9].getValue();
            int[] rgb = Converter.cmykToRgb(c, m, y, k);
            r = rgb[0]; g = rgb[1]; b = rgb[2];
            double[] hsv = Converter.rgbToHsv(r, g, b, currentH, currentS);
            currentH = hsv[0]; currentS = hsv[1]; currentV = hsv[2];
        }

        updateOtherModels(idx);
        isUpdating = false;
    }

private void updateOtherModels(int skipIdx) {
        double[] cmyk = Converter.rgbToCmyk(r, g, b);
        double[] hsv = Converter.rgbToHsv(r, g, b, currentH, currentS);
        
        // Главная защита от скачков: если яркость больше нуля, 
        // обновляем оттенок и насыщенность из формулы. 
        // Если яркость = 0, принудительно сохраняем прошлые S и H, чтобы они не дергались.
        if (hsv[2] > 0.001) {
            currentH = hsv[0];
            if (hsv[1] > 0.001 || currentV > 0) {
                currentS = hsv[1];
            }
        }
        
        currentV = hsv[2]; // Яркость всегда обновляем честно

        int[] values = {
            (int) Math.round(currentH), 
            (int) Math.round(currentS), 
            (int) Math.round(currentV),
            r, g, b,
            (int) Math.round(cmyk[0]), 
            (int) Math.round(cmyk[1]), 
            (int) Math.round(cmyk[2]), 
            (int) Math.round(cmyk[3])
        };

        for (int i = 0; i < values.length; i++) {
            if (i != skipIdx) {
                sliders[i].setValue(values[i]);
                fields[i].setText(String.valueOf(values[i]));
            }
        }
        drawSvSquare();
        previewPanel.setBackground(new Color(r, g, b));
    }

    private void updateAllFromRgb(int newR, int newG, int newB) {
        r = newR; g = newG; b = newB;
        double[] hsv = Converter.rgbToHsv(r, g, b, currentH, currentS);
        currentH = hsv[0]; 
        currentS = hsv[1]; 
        currentV = hsv[2];
        updateOtherModels(-1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ColorPickerApp().setVisible(true));
    }
}