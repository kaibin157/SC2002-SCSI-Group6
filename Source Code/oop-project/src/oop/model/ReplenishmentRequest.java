package oop.model;

/**
 * Represents a request for replenishment of a medication in the system.
 * The request includes the medication name, amount requested, and its current status.
 */
public class ReplenishmentRequest {

    private String medicationName;
    private int amount;
    private String status;  // pending or approved

    /**
     * Constructs a new ReplenishmentRequest with the specified medication name and amount.
     * The status is set to "pending" by default.
     *
     * @param medicationName the name of the medication to be replenished
     * @param amount the amount of medication requested
     */
    public ReplenishmentRequest(String medicationName, int amount) {
        this.medicationName = medicationName;
        this.amount = amount;
        this.status = "pending";  // Default status is pending
    }

    /**
     * Gets the name of the medication.
     *
     * @return the medication name
     */
    public String getMedicationName() {
        return medicationName;
    }

    /**
     * Gets the amount of medication requested.
     *
     * @return the amount requested
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gets the status of the replenishment request.
     * The status can be "pending" or "approved".
     *
     * @return the current status of the request
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the replenishment request.
     * The status can be updated to indicate approval or other states.
     *
     * @param status the new status to be set (e.g., "approved")
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
