package oop.model;

/**
 * The Medication class represents a medication with details such as name, stock level, low stock alert threshold, and status.
 * This class can be used for both inventory management and prescribed medications for appointments.
 */
public class Medication {

    /** The name of the medication. */
    private String name;

    /** The current stock level of the medication. */
    private int stockLevel;

    /** The threshold at which a low stock alert is triggered. */
    private int lowStockAlert;

    /** The status of the medication (e.g., pending, dispensed). */
    private String status;

    /**
     * Constructs a Medication object for inventory management purposes with all details.
     *
     * @param name         The name of the medication.
     * @param stockLevel   The current stock level of the medication.
     * @param lowStockAlert The threshold for triggering a low stock alert.
     */
    public Medication(String name, int stockLevel, int lowStockAlert) {
        this.name = name;
        this.stockLevel = stockLevel;
        this.lowStockAlert = lowStockAlert;
        this.status = "pending";  // Default status is set to "pending"
    }

    /**
     * Constructs a Medication object with just the name, for cases such as recording prescribed medications.
     *
     * @param name The name of the medication.
     */
    public Medication(String name) {
        this.name = name;
        this.status = "pending";  // Default status is set to "pending"
    }

    /**
     * Gets the name of the medication.
     *
     * @return The name of the medication.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the current stock level of the medication.
     *
     * @return The stock level of the medication.
     */
    public int getStockLevel() {
        return stockLevel;
    }

    /**
     * Sets the current stock level of the medication.
     *
     * @param stockLevel The new stock level of the medication.
     */
    public void setStockLevel(int stockLevel) {
        this.stockLevel = stockLevel;
    }

    /**
     * Gets the low stock alert threshold for the medication.
     *
     * @return The low stock alert threshold.
     */
    public int getLowStockAlert() {
        return lowStockAlert;
    }

    /**
     * Gets the status of the medication (e.g., pending, dispensed).
     *
     * @return The status of the medication.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the medication (e.g., pending, dispensed).
     *
     * @param status The new status of the medication.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Provides a string representation of the medication, showing the name and status.
     *
     * @return A string with medication details.
     */
    @Override
    public String toString() {
        return "Medication: " + name + " | Status: " + status;
    }
}
