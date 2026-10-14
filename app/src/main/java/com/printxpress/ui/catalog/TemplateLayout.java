package com.printxpress.ui.catalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemplateLayout {
    public List<TemplateField> fields;
    public boolean hasLogo;
    public boolean hasQrCode;
    public boolean hasPhoto;

    public TemplateLayout(List<TemplateField> fields, boolean hasLogo, boolean hasQrCode, boolean hasPhoto) {
        this.fields = fields;
        this.hasLogo = hasLogo;
        this.hasQrCode = hasQrCode;
        this.hasPhoto = hasPhoto;
    }

    public static TemplateLayout getLayoutForTemplate(String templateTitle) {
        if (templateTitle == null) templateTitle = "";
        String lower = templateTitle.toLowerCase();

        if (lower.contains("t-shirt") || lower.contains("tee") || lower.contains("jersey") || lower.contains("uniform")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("slogan", "Slogan text", "Slogan", "Dream Big. Work Hard.", "text", true, 40),
                new TemplateField("subline", "Sub-line", "Sub-line", "Stay Humble", "text", false, 40)
            )), false, false, false);
        }

        if (lower.contains("mug")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("name", "Name", "Name", "SAMANTHA", "text", true, 30),
                new TemplateField("message", "Short message", "Message", "World's Best Coffee Lover", "text", false, 50)
            )), true, false, false);
        }

        if (lower.contains("poster")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("title", "Event title", "Enter event title", "Music Night 2026", "text", true, 50),
                new TemplateField("tagline", "Tagline", "Enter tagline", "Live on stage", "text", false, 60),
                new TemplateField("date", "Date", "Select date", "25 December 2026", "text", true, 30),
                new TemplateField("time", "Time", "Enter time", "7:00 PM", "text", true, 30),
                new TemplateField("venue", "Venue", "Enter venue", "Colombo Open Air Theatre", "text", true, 50),
                new TemplateField("contact", "Contact", "Enter phone", "0712345678", "phone", true, 15)
            )), true, true, false);
        }

        if (lower.contains("banner") || lower.contains("sign") || lower.contains("opening")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("shop", "Shop name", "Shop name", "New Tech Solutions", "text", true, 40),
                new TemplateField("headline", "Headline", "Headline", "Grand Opening", "text", true, 30),
                new TemplateField("date", "Date", "Date", "This Saturday 9 AM", "text", true, 30),
                new TemplateField("address", "Address", "Address", "45 Galle Road, Colombo", "text", true, 60),
                new TemplateField("contact", "Contact", "Phone", "0712345678", "phone", true, 15)
            )), true, true, false);
        }

        if (lower.contains("brochure") || lower.contains("tri-fold") || lower.contains("trifold")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("contact_header", "Left Panel: Header", "Header", "CONTACT US", "text", true, 30),
                new TemplateField("phone", "Left Panel: Phone", "Phone", "+94 11 2345678", "phone", true, 20),
                new TemplateField("email", "Left Panel: Email", "Email", "info@apexcorp.lk", "email", true, 40),
                new TemplateField("website", "Left Panel: Website", "Website", "www.apexcorp.lk", "url", true, 40),
                new TemplateField("address", "Left Panel: Address", "Address", "Colombo 03, Sri Lanka", "text", false, 50),
                new TemplateField("main_title", "Middle Panel: Title", "Title", "TRIFOLD BROCHURE", "text", true, 40),
                new TemplateField("tagline", "Middle Panel: Tagline", "Tagline", "ABSTRACT TRI-FOLD DESIGN", "text", true, 50),
                new TemplateField("company", "Middle Panel: Company", "Company", "APEX PRINT STUDIO", "text", true, 40),
                new TemplateField("service_header", "Right Panel: Header", "Header", "OUR SERVICES", "text", true, 30),
                new TemplateField("features", "Right Panel: Services List", "Services", "01 Digital Offset • 02 Custom Apparel • 03 Express Shipping", "text", true, 100)
            )), true, true, false);
        }

        if (lower.contains("flyer") || lower.contains("menu")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("restaurant", "Business / Restaurant Name", "Name", "Spice Garden Studio", "text", true, 40),
                new TemplateField("items", "Promo Details & Pricing", "Details", "Special Meal Combo - Rs. 750 / Free Delivery", "text", true, 100),
                new TemplateField("hours", "Hours / Validity", "Hours", "Open Mon - Sat: 9 AM - 9 PM", "text", true, 30),
                new TemplateField("phone", "Contact Phone", "Phone", "+94 77 123 4567", "phone", true, 15)
            )), true, true, false);
        }

        if (lower.contains("calendar")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("year_month", "Year & Month", "Year Month", "2026 JANUARY", "text", true, 30),
                new TemplateField("company", "Company / Brand Name", "Company", "APEX PRINT STUDIO", "text", true, 40),
                new TemplateField("calendar_grid", "Month Dates Grid", "Dates Grid", "Sun  Mon  Tue  Wed  Thu  Fri  Sat\n                        1     2     3\n  4     5     6     7     8     9    10\n 11   12   13   14   15   16   17\n 18   19   20   21   22   23   24\n 25   26   27   28   29   30   31", "text", true, 200),
                new TemplateField("contact", "Contact & Address", "Contact", "www.apexcorp.lk | +94 11 2345678", "text", false, 50)
            )), true, true, false);
        }

        // Specific Sticker templates
        if (lower.contains("logo") || lower.contains("die-cut")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("brand", "Brand name", "Brand", "Apex Studio", "text", true, 30),
                new TemplateField("tagline", "Tagline", "Tagline", "Handmade with Love", "text", false, 40)
            )), true, false, false);
        }

        if (lower.contains("round") || lower.contains("label")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("product", "Product name", "Product", "Organic Honey Jar", "text", true, 30),
                new TemplateField("weight", "Weight / Size", "Size", "Net Wt. 250g", "text", true, 20)
            )), true, false, false);
        }

        if (lower.contains("quote") || lower.contains("funny")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("quote", "Quote text", "Quote", "Keep Smiling :)", "text", true, 60)
            )), false, false, false);
        }

        if (lower.contains("name tag") || lower.contains("badge")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("name", "Name", "Name", "Amal Perera", "text", true, 30),
                new TemplateField("designation", "Designation", "Designation", "Sales Executive", "text", false, 40)
            )), true, false, false);
        }

        if (lower.contains("seal") || lower.contains("packaging")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("brand", "Brand name", "Brand", "PrintXpress", "text", true, 30),
                new TemplateField("message", "Message", "Message", "Thank you for your order!", "text", false, 50)
            )), true, false, false);
        }

        if (lower.contains("sticker")) {
            return new TemplateLayout(new ArrayList<>(Arrays.asList(
                new TemplateField("brand", "Brand name", "Brand", "PrintXpress", "text", true, 30),
                new TemplateField("line", "Short line", "Line", "Fresh & Natural", "text", false, 40)
            )), true, false, false);
        }

        // Default / Business Cards
        return new TemplateLayout(new ArrayList<>(Arrays.asList(
            new TemplateField("name", "Full Name", "Enter full name", "John Doe", "text", true, 40),
            new TemplateField("title", "Job Title", "Enter title", "Senior Executive", "text", false, 40),
            new TemplateField("company", "Company", "Enter company", "PRINTXPRESS LTD", "text", false, 40),
            new TemplateField("phone", "Phone", "Enter phone", "0712345678", "phone", true, 15),
            new TemplateField("email", "Email", "Enter email", "john@example.com", "email", false, 50),
            new TemplateField("website", "Website", "Enter URL", "www.printxpress.lk", "url", false, 50),
            new TemplateField("address", "Address", "Enter address", "Colombo, Sri Lanka", "text", false, 60)
        )), true, true, false);
    }
}
