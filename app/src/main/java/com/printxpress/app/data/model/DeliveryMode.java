package com.printxpress.app.data.model;

public enum DeliveryMode {
    PICKUP("Store Pickup"),
    DELIVERY("Home Delivery");

    private final String label;

    DeliveryMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
