package oop.model;

import oop.HMS;

import java.io.IOException;
import java.util.Scanner;

/**
 * The Pharmacist class represents a user in the role of a pharmacist
 * within the hospital management system (HMS). This class provides methods
 * to view and update records related to prescriptions, manage inventory,
 * and handle various pharmacy-related tasks.
 */
public class Pharmacist extends User {
    private String gender;
    private String age;

    /**
     * Constructs a new Pharmacist instance with the specified details.
     *
     * @param hospitalID The unique hospital ID of the pharmacist.
     * @param password   The password for the pharmacist's account.
     * @param name       The name of the pharmacist.
     * @param gender     The gender of the pharmacist.
     * @param age        The age of the pharmacist.
     */
    public Pharmacist(String hospitalID, String password, String name, String gender, String age) {
        super(hospitalID, password, name);
        this.gender = gender;
        this.age = age;
    }

    /**
     * Gets the gender of the pharmacist.
     *
     * @return The pharmacist's gender.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Gets the age of the pharmacist.
     *
     * @return The pharmacist's age.
     */
    public String getAge() {
        return age;
    }

    /**
     * Displays the pharmacist's menu and processes their menu selection.
     * Provides various options related to appointment outcomes, prescription status,
     * medication inventory, and other relevant pharmacist operations.
     *
     * @param hms The hospital management system instance to interact with.
     */
    @Override
    public void displayMenu(HMS hms) {
        Scanner scanner = new Scanner(System.in);  // Create scanner inside the method

        while (true) {
            System.out.println("Pharmacist Menu:");
            System.out.println("1. View Appointment Outcome Record");
            System.out.println("2. Update Prescription Status");
            System.out.println("3. View Medication Inventory");
            System.out.println("4. Submit Replenishment Request");
            System.out.println("5. View Replenishment Requests");
            System.out.println("6. Send Invoice");
            System.out.println("7. Change Password");
            System.out.println("8. Logout");

            int choice = -1;

            // Validate input to ensure it's an integer within the valid range (1-8)
            while (true) {
                System.out.print("Enter your choice (1-8): ");

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();  // Consume newline

                    if (choice >= 1 && choice <= 8) {
                        break;  // Valid input
                    } else {
                        System.out.println("Invalid choice. Please enter a number between 1 and 8.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.next();  // Clear invalid input
                }
            }

            // Execute actions based on user choice
            switch (choice) {
                case 1:
                    try {
                        hms.viewAppointmentOutcomeRecord();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        hms.updatePrescriptionStatus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    try {
                        hms.viewMedicationInventory();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    try {
                        hms.submitReplenishmentRequest(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 5:
                    try {
                        hms.viewReplenishmentRequests(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 6:
                    try {
                        hms.sendInvoice(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 7:
                    try {
                        hms.changePassword(this, scanner);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 8:
                    System.out.println("Logging out...");
                    return;  // Logout
                default:
                    System.out.println("Unexpected error.");
            }
        }
    }
}
