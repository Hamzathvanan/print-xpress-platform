package com.printxpress.app.data.model;

public enum OrderStatus {
    RECEIVED("Received"),
    PROCESSING("Processing"),
    PRINTING("Printing"),
    READY_FOR_PICKUP("Ready for Pickup"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
