package com.printxpress.app.data.model;

public enum ProductCategory {
    BUSINESS_CARDS("Business Cards"),
    FLYERS("Flyers"),
    POSTERS("Posters"),
    BANNERS("Banners"),
    STICKERS("Stickers"),
    MUGS("Mugs"),
    T_SHIRTS("T-Shirts");

    private final String label;

    ProductCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
