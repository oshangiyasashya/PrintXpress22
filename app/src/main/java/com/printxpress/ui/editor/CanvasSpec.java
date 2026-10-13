package com.printxpress.ui.editor;

public class CanvasSpec {
    public long categoryId;
    public String categoryName;
    public float widthMm;
    public float heightMm;
    public boolean hasBleed;
    public String shapeType; // "rectangle", "circle", "mug", "tshirt"

    public CanvasSpec(long categoryId, String categoryName, float widthMm, float heightMm, boolean hasBleed, String shapeType) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.widthMm = widthMm;
        this.heightMm = heightMm;
        this.hasBleed = hasBleed;
        this.shapeType = shapeType;
    }

    public static CanvasSpec getSpecForCategory(long categoryId) {
        switch ((int) categoryId) {
            case 1: // Business Cards (90x55 mm)
                return new CanvasSpec(1, "Business Cards", 90f, 55f, true, "rectangle");
            case 2: // Flyers (A5: 148x210 mm)
                return new CanvasSpec(2, "Flyers", 148f, 210f, true, "rectangle");
            case 3: // Posters (A4: 210x297 mm)
                return new CanvasSpec(3, "Posters", 210f, 297f, true, "rectangle");
            case 4: // Banners & Signs (180x90 mm ratio)
                return new CanvasSpec(4, "Banners & Signs", 180f, 90f, true, "rectangle");
            case 5: // Stickers & Labels (50x50 mm)
                return new CanvasSpec(5, "Stickers & Labels", 50f, 50f, true, "circle");
            case 6: // Mugs (Wrap area: 210x90 mm)
                return new CanvasSpec(6, "Mugs", 210f, 90f, false, "mug");
            case 7: // T-Shirts (Print area: 280x350 mm)
                return new CanvasSpec(7, "T-Shirts", 280f, 350f, false, "tshirt");
            case 8: // Brochures
                return new CanvasSpec(8, "Brochures", 210f, 297f, true, "rectangle");
            case 9: // Calendars
                return new CanvasSpec(9, "Calendars", 297f, 210f, true, "rectangle");
            default:
                return new CanvasSpec(10, "Custom Order", 100f, 100f, true, "rectangle");
        }
    }
}
