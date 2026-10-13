package com.printxpress.ui.catalog;

public class TemplateField {
    public String key;
    public String label;
    public String placeholder;
    public String defaultValue;
    public String inputType;
    public boolean required;
    public int maxLength;

    public TemplateField(String key, String label, String placeholder, String defaultValue, String inputType, boolean required, int maxLength) {
        this.key = key;
        this.label = label;
        this.placeholder = placeholder;
        this.defaultValue = defaultValue;
        this.inputType = inputType;
        this.required = required;
        this.maxLength = maxLength;
    }
}
