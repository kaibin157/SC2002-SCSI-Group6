package oop.model;

import oop.HMS;

import java.io.IOException;
import java.util.Scanner;

/**
 * The Administrator class represents an administrator user in the system,
 * extending the User class and adding specific attributes and functionalities
 * such as gender, age, and an administrator-specific menu.
 */
public class Administrator extends User {
    /** The gender of the administrator. */
    private String gender;

    /** The age of the administrator. */
    private String age;

    /**
     * Constructs an Administrator with the specified details.
     *
     * @param hospitalID The hospital ID of the administrator.
     * @param password   The password for the administrator account.
     * @param name       The name of the administrator.
     * @param gender     The gender of the administrator.
     * @param age        The age of the administrator.
     */
    public Administrator(String hospitalID, String password, String name, String gender, String age) {
        super(hospitalID, password, name);
        this.gender = gender;
        this.age = age;
    }

    /**
     * Returns the gender of the administrator.
     *
     * @return The gender of the administrator.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Returns the age of the administrator.
     *
     * @return The age of the administrator.
     */
    public String getAge() {
        return age;
    }

    /**
     * Displays the administrator menu and performs actions based on the selected option.
     * Allows the administrator to manage hospital staff, view appointments, manage inventory,
     * approve requests, and change their password.
     *
     * @param hms An instance of the HMS (Hospital Management System) class for accessing
     *            hospital-related functionalities.
     */
    public void displayMenu(HMS hms) {
        Scanner scanner = new Scanner(System.in);  // Scanner instantiated inside the method

        while (true) {
            System.out.println("Administrator Menu:");
            System.out.println("1. View and Manage Hospital Staff");
            System.out.println("2. View Appointments Details");
            System.out.println("3. Manage Medication Inventory");
            System.out.println("4. Approve Replenishment Requests");
            System.out.println("5. Change Password");
            System.out.println("6. Logout");

            int choice = -1;

            while (true) {
                System.out.print("Enter your choice (1-6): ");

                // Check if input is an integer
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();  // Consume newline

                    // Check if choice is in the valid range
                    if (choice >= 1 && choice <= 6) {
                        break;  // Valid input
                    } else {
                        System.out.println("Invalid choice. Please enter a number between 1 and 6.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.next();  // Clear invalid input
                }
            }

            try {
                switch (choice) {
                    case 1:
                        hms.manageHospitalStaff();
                        break;
                    case 2:
                        hms.viewAppointmentDetails();
                        break;
                    case 3:
                        hms.manageMedicationInventory(scanner);
                        break;
                    case 4:
                        hms.approveReplenishmentRequests();  // Approve Replenishment Requests
                        break;
                    case 5:
                        try {
                            hms.changePassword(this, scanner);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 6:
                        System.out.println("Logging out...");
                        return;  // Logout
                    default:
                        System.out.println("Unexpected error.");
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
