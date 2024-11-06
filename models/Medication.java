package oop.models;

public class Medication {
    private String name;
    private int stockLevel;
    private int lowStockAlert;
    private String status;  // e.g., pending, dispensed

    // Constructor for full medication details (used for inventory management)
    public Medication(String name, int stockLevel, int lowStockAlert) {
        this.name = name;
        this.stockLevel = stockLevel;
        this.lowStockAlert = lowStockAlert;
        this.status = "pending";  // Default status is set to "pending"
    }

    // Simplified constructor for prescribed medications (used for appointment outcomes)
    public Medication(String name) {
        this.name = name;
        this.status = "pending";  // Default status is set to "pending"
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public int getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(int stockLevel) {
        this.stockLevel = stockLevel;
    }

    public int getLowStockAlert() {
        return lowStockAlert;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Medication: " + name + " | Status: " + status;
    }
}
