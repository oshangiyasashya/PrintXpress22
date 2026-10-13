package com.printxpress.ui.catalog;

import java.util.Arrays;
import java.util.List;

public class CategoryConfig {
    public long categoryId;
    public String categoryName;
    public float canvasAspectRatio; // width / height
    public List<String> availableSizes;
    public List<String> orientationOptions;
    public boolean supportDoubleSided;
    public List<String> materialOptions;
    public List<String> defaultFields; // e.g. ["Full Name", "Job Title", "Company", "Phone", "Email"]

    public CategoryConfig(long categoryId, String categoryName, float canvasAspectRatio, List<String> availableSizes, List<String> orientationOptions, boolean supportDoubleSided, List<String> materialOptions, List<String> defaultFields) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.canvasAspectRatio = canvasAspectRatio;
        this.availableSizes = availableSizes;
        this.orientationOptions = orientationOptions;
        this.supportDoubleSided = supportDoubleSided;
        this.materialOptions = materialOptions;
        this.defaultFields = defaultFields;
    }

    public static CategoryConfig getConfigForCategory(long categoryId) {
        switch ((int) categoryId) {
            case 1: // Business Cards
                return new CategoryConfig(1, "Business Cards", 1.8f,
                        Arrays.asList("Standard 90x50 mm", "US 3.5x2 in", "Square 55x55 mm"),
                        Arrays.asList("Landscape", "Portrait"), true,
                        Arrays.asList("350gsm Matte Artboard", "Gloss Lamination"),
                        Arrays.asList("Full Name", "Job Title", "Company", "Phone", "Email", "Website", "Address"));
            case 2: // Posters
                return new CategoryConfig(2, "Posters", 0.7f,
                        Arrays.asList("A3", "A2", "A1"),
                        Arrays.asList("Portrait", "Landscape"), false,
                        Arrays.asList("200gsm Gloss Paper", "300gsm Heavy Matt"),
                        Arrays.asList("Event Title", "Subtitle / Offer", "Date & Time", "Location / Venue", "Contact Info"));
            case 3: // Banners
                return new CategoryConfig(3, "Banners & Signs", 2.5f,
                        Arrays.asList("2x6 ft", "3x8 ft", "4x10 ft"),
                        Arrays.asList("Landscape"), false,
                        Arrays.asList("Heavy-duty Vinyl", "Mesh Vinyl", "Eyelets & Hem"),
                        Arrays.asList("Main Headline", "Event Details", "Phone Number", "Website / Social"));
            case 4: // Flyers
                return new CategoryConfig(4, "Flyers & Brochures", 0.7f,
                        Arrays.asList("A5", "A4"),
                        Arrays.asList("Portrait", "Landscape"), true,
                        Arrays.asList("150gsm Gloss Art Paper", "250gsm Premium"),
                        Arrays.asList("Headline", "Promo Details", "Valid Until", "Contact Info"));
            case 5: // Stickers
                return new CategoryConfig(5, "Stickers", 1.0f,
                        Arrays.asList("Circle", "Square", "Rounded Rectangle", "Die-Cut"),
                        Arrays.asList("Standard"), false,
                        Arrays.asList("Waterproof Vinyl", "Gloss Paper Sticker"),
                        Arrays.asList("Brand Name", "Tagline", "Product Info", "Website"));
            case 6: // Mugs
                return new CategoryConfig(6, "Mugs", 2.2f,
                        Arrays.asList("11oz Standard", "15oz Large"),
                        Arrays.asList("Wrap-around"), false,
                        Arrays.asList("Classic Ceramic", "Magic Color Changing"),
                        Arrays.asList("Custom Name", "Quote / Message", "Year / Date"));
            case 7: // T-Shirts
                return new CategoryConfig(7, "Custom T-Shirts", 0.8f,
                        Arrays.asList("Small (S)", "Medium (M)", "Large (L)", "XL", "XXL"),
                        Arrays.asList("Front & Back"), true,
                        Arrays.asList("100% Combed Cotton", "Poly-Cotton Blend"),
                        Arrays.asList("Team / Brand Name", "Slogan / Number", "Event Name"));
            default:
                return getConfigForCategory(1);
        }
    }
}
