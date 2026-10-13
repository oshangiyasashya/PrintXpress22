package com.printxpress.ui.colorpicker;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorUtils {

    // Validate hex color string (e.g. #RRGGBB or #AARRGGBB or RRGGBB)
    public static boolean isValidHexColor(String colorStr) {
        if (colorStr == null) return false;
        String hex = colorStr.trim();
        if (hex.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8}|[A-Fa-f0-9]{3})$")) {
            return true;
        }
        return hex.matches("^([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8}|[A-Fa-f0-9]{3})$");
    }

    // Convert color int to web hex string (#RRGGBB)
    public static String colorToHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    // Parse color string safely with fallback
    public static int parseColor(String colorStr, int fallback) {
        if (isValidHexColor(colorStr)) {
            String hex = colorStr.trim();
            if (!hex.startsWith("#")) {
                hex = "#" + hex;
            }
            try {
                return Color.parseColor(hex);
            } catch (IllegalArgumentException e) {
                return fallback;
            }
        }
        return fallback;
    }

    // HSV to Color int
    public static int hsvToColor(float[] hsv) {
        return Color.HSVToColor(hsv);
    }

    // Color int to HSV
    public static void colorToHsv(int color, float[] hsv) {
        Color.colorToHSV(color, hsv);
    }

    // Generate standard color palette swatches (material colors, hues, greys)
    public static List<Integer> getDefaultPaletteColors() {
        List<Integer> colors = new ArrayList<>();
        // Greys & Monochrome
        colors.add(Color.parseColor("#000000"));
        colors.add(Color.parseColor("#333333"));
        colors.add(Color.parseColor("#666666"));
        colors.add(Color.parseColor("#999999"));
        colors.add(Color.parseColor("#CCCCCC"));
        colors.add(Color.parseColor("#FFFFFF"));

        // Primary / Material Hues
        colors.add(Color.parseColor("#EF4444")); // Red
        colors.add(Color.parseColor("#F97316")); // Orange
        colors.add(Color.parseColor("#F59E0B")); // Amber / Gold
        colors.add(Color.parseColor("#10B981")); // Emerald
        colors.add(Color.parseColor("#06B6D4")); // Cyan
        colors.add(Color.parseColor("#3B82F6")); // Blue
        colors.add(Color.parseColor("#6366F1")); // Indigo
        colors.add(Color.parseColor("#8B5CF6")); // Purple
        colors.add(Color.parseColor("#EC4899")); // Pink
        colors.add(Color.parseColor("#1E3A8A")); // Deep Navy
        colors.add(Color.parseColor("#0F172A")); // Slate Dark
        colors.add(Color.parseColor("#B91C1C")); // Dark Red
        colors.add(Color.parseColor("#C2410C")); // Dark Orange
        colors.add(Color.parseColor("#047857")); // Dark Green
        return colors;
    }

    // Recent colors storage (in-memory list with max size)
    private static final List<Integer> recentColors = new ArrayList<>();
    private static final int MAX_RECENT = 12;

    public static synchronized void addRecentColor(int color) {
        recentColors.remove(Integer.valueOf(color));
        recentColors.add(0, color);
        if (recentColors.size() > MAX_RECENT) {
            recentColors.remove(recentColors.size() - 1);
        }
    }

    public static synchronized List<Integer> getRecentColors() {
        if (recentColors.isEmpty()) {
            recentColors.add(Color.BLACK);
            recentColors.add(Color.WHITE);
            recentColors.add(Color.parseColor("#3B82F6"));
            recentColors.add(Color.parseColor("#F59E0B"));
            recentColors.add(Color.parseColor("#EF4444"));
        }
        return new ArrayList<>(recentColors);
    }
}
