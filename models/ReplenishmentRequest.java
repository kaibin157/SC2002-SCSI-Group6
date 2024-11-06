package oop.models;

public class ReplenishmentRequest {
    private String medicationName;
    private int amount;
    private String status;  // pending or approved

    public ReplenishmentRequest(String medicationName, int amount) {
        this.medicationName = medicationName;
        this.amount = amount;
        this.status = "pending";  // Default status is pending
    }

    public String getMedicationName() {
        return medicationName;
    }

    public int getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

