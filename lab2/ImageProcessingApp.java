import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImageProcessingApp extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    
    private JLabel lblOriginal;
    private JLabel lblProcessed;
    private JComboBox<String> cbMethods;

    public ImageProcessingApp() {
        setTitle("Lab 2 Вариант 14");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        JButton btnLoad = new JButton("Загрузить изображение");
        btnLoad.addActionListener(e -> loadImage());

        cbMethods = new JComboBox<>(new String[]{
            "1. Глобальный порог (Фиксированный)", 
            "2. Метод Оцу (Автоматический глобальный порог)",
            "3. Выделение границ (Оператор Собеля/Перепады)"
        });

        JButton btnApply = new JButton("Применить метод");
        btnApply.addActionListener(e -> applyProcessing());

        controlPanel.add(btnLoad);
        controlPanel.add(new JLabel("Метод:"));
        controlPanel.add(cbMethods);
        controlPanel.add(btnApply);
        add(controlPanel, BorderLayout.NORTH);

        // Панель для отображения картинок (До и После)
        JPanel imagesPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        imagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblOriginal = new JLabel("Исходное изображение", JLabel.CENTER);
        lblOriginal.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblProcessed = new JLabel("Результат обработки", JLabel.CENTER);
        lblProcessed.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        imagesPanel.add(lblOriginal);
        imagesPanel.add(lblProcessed);
        add(imagesPanel, BorderLayout.CENTER);
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(file);
                lblOriginal.setIcon(new ImageIcon(originalImage.getScaledInstance(420, 420, Image.SCALE_SMOOTH)));
                processedImage = null;
                lblProcessed.setIcon(null);
                lblProcessed.setText("Результат обработки");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки файла!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyProcessing() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите изображение!", "Предупреждение", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int methodIndex = cbMethods.getSelectedIndex();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // Создаем копию для результата в оттенках серого
        BufferedImage grayImg = toGrayscale(originalImage);
        BufferedImage resImg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        if (methodIndex == 0) {
            // Фиксированный глобальный порог (например, 128)
            int threshold = 128;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int p = grayImg.getRGB(x, y) & 0xFF;
                    int binaryVal = (p > threshold) ? 255 : 0;
                    resImg.setRGB(x, y, new Color(binaryVal, binaryVal, binaryVal).getRGB());
                }
            }
        } else if (methodIndex == 1) {
            int threshold = calculateOtsuThreshold(grayImg);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int p = grayImg.getRGB(x, y) & 0xFF;
                    int binaryVal = (p > threshold) ? 255 : 0;
                    resImg.setRGB(x, y, new Color(binaryVal, binaryVal, binaryVal).getRGB());
                }
            }
        } else if (methodIndex == 2) {
            resImg = applySobelFilter(grayImg);
        }

        processedImage = resImg;
        lblProcessed.setIcon(new ImageIcon(processedImage.getScaledInstance(420, 420, Image.SCALE_SMOOTH)));
    }

    private BufferedImage toGrayscale(BufferedImage img) {
        BufferedImage gray = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = gray.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return gray;
    }

    private int calculateOtsuThreshold(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int total = width * height;
        int[] histogram = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = img.getRGB(x, y) & 0xFF;
                histogram[val]++;
            }
        }

        float sum = 0;
        for (int i = 0; i < 256; i++) sum += i * histogram[i];

        float sumB = 0;
        int wB = 0;
        int wF;
        float varMax = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            wF = total - wB;
            if (wF == 0) break;

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }
        return threshold;
    }

    private BufferedImage applySobelFilter(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int[][] gx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] gy = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumX = 0;
                int sumY = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int val = img.getRGB(x + kx, y + ky) & 0xFF;
                        sumX += val * gx[ky + 1][kx + 1];
                        sumY += val * gy[ky + 1][kx + 1];
                    }
                }

                int magnitude = (int) Math.min(255, Math.sqrt(sumX * sumX + sumY * sumY));
                result.setRGB(x, y, new Color(magnitude, magnitude, magnitude).getRGB());
            }
        }
        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ImageProcessingApp().setVisible(true);
        });
    }
}