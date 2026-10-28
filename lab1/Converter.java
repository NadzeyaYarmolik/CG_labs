// Converter.java
public class Converter {

    public static double[] rgbToCmyk(int r, int g, int b) {
        if (r == 0 && g == 0 && b == 0) {
            return new double[]{0.0, 0.0, 0.0, 100.0};
        }
        double rN = r / 255.0;
        double gN = g / 255.0;
        double bN = b / 255.0;
        
        double k = 1.0 - Math.max(rN, Math.max(gN, bN));
        double c = (1.0 - rN - k) / (1.0 - k);
        double m = (1.0 - gN - k) / (1.0 - k);
        double y = (1.0 - bN - k) / (1.0 - k);
        
        return new double[]{
            Math.round(c * 100.0 * 10.0) / 10.0,
            Math.round(m * 100.0 * 10.0) / 10.0,
            Math.round(y * 100.0 * 10.0) / 10.0,
            Math.round(k * 100.0 * 10.0) / 10.0
        };
    }

    public static int[] cmykToRgb(double c, double m, double y, double k) {
        double cN = c / 100.0;
        double mN = m / 100.0;
        double yN = y / 100.0;
        double kN = k / 100.0;

        int r = (int) Math.round(255 * (1.0 - cN) * (1.0 - kN));
        int g = (int) Math.round(255 * (1.0 - mN) * (1.0 - kN));
        int b = (int) Math.round(255 * (1.0 - yN) * (1.0 - kN));

        return new int[]{
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b))
        };
    }

    // Принимаем текущие H и S, чтобы они не терялись при V=0 или S=0
    public static double[] rgbToHsv(int r, int g, int b, double currentH, double currentS) {
        double rN = r / 255.0;
        double gN = g / 255.0;
        double bN = b / 255.0;

        double cmax = Math.max(rN, Math.max(gN, bN));
        double cmin = Math.min(rN, Math.min(gN, bN));
        double diff = cmax - cmin;
        
        double h = currentH;
        double s = currentS;
        double v = cmax * 100.0;

        if (diff != 0) {
            if (cmax == rN) {
                h = (60.0 * ((gN - bN) / diff) + 360.0) % 360.0;
            } else if (cmax == gN) {
                h = (60.0 * ((bN - rN) / diff) + 120.0) % 360.0;
            } else {
                h = (60.0 * ((rN - gN) / diff) + 240.0) % 360.0;
            }
        }

        if (cmax != 0) {
            s = (diff / cmax) * 100.0;
        }

        return new double[]{h, s, v};
    }

    public static int[] hsvToRgb(double h, double s, double v) {
        double sN = s / 100.0;
        double vN = v / 100.0;
        double c = vN * sN;
        double x = c * (1.0 - Math.abs((h / 60.0) % 2.0 - 1.0));
        double m = vN - c;

        double rP = 0, gP = 0, bP = 0;
        if (h >= 0 && h < 60) {
            rP = c; gP = x; bP = 0;
        } else if (h >= 60 && h < 120) {
            rP = x; gP = c; bP = 0;
        } else if (h >= 120 && h < 180) {
            rP = 0; gP = c; bP = x;
        } else if (h >= 180 && h < 240) {
            rP = 0; gP = x; bP = c;
        } else if (h >= 240 && h < 300) {
            rP = x; gP = 0; bP = c;
        } else if (h >= 300 && h <= 360) {
            rP = c; gP = 0; bP = x;
        }

        int r = (int) Math.round((rP + m) * 255.0);
        int g = (int) Math.round((gP + m) * 255.0);
        int b = (int) Math.round((bP + m) * 255.0);

        return new int[]{
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b))
        };
    }
}