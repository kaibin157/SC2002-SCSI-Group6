package oop.model;

import oop.HMS;

import java.util.Scanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Doctor class represents a doctor in the system, inheriting from the User class.
 * This class manages the doctor's personal information, availability, and actions
 * that the doctor can perform, such as viewing and updating patient records, managing appointments, etc.
 */
public class Doctor extends User {

    /** The doctor's gender. */
    private String gender;

    /** The doctor's age. */
    private String age;

    /** List to store the doctor's available dates and times for appointments. */
    private List<String> availability;

    /**
     * Constructs a Doctor with the specified details.
     *
     * @param hospitalID The unique ID assigned to the doctor.
     * @param password   The password for the doctor's account.
     * @param name       The doctor's name.
     * @param gender     The doctor's gender.
     * @param age        The doctor's age.
     */
    public Doctor(String hospitalID, String password, String name, String gender, String age) {
        super(hospitalID, password, name);
        this.gender = gender;
        this.age = age;
        this.availability = new ArrayList<>();  // Initialize availability list
    }

    /**
     * Gets the doctor's gender.
     *
     * @return The doctor's gender.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Gets the doctor's age.
     *
     * @return The doctor's age.
     */
    public String getAge() {
        return age;
    }

    /**
     * Adds a new available slot to the doctor's availability.
     *
     * @param availabilitySlot The date and time slot to add.
     */
    public void setAvailability(String availabilitySlot) {
        this.availability.add(availabilitySlot);
    }

    /**
     * Retrieves the list of the doctor's available slots.
     *
     * @return A list of available date and time slots.
     */
    public List<String> getAvailability() {
        return availability;
    }

    /**
     * Removes a booked slot from the doctor's availability.
     *
     * @param slot The slot to be removed after being booked.
     */
    public void removeAvailability(String slot) {
        this.availability.remove(slot);  // Remove slot once it's booked
    }

    /**
     * Displays the menu for the doctor, allowing them to perform various actions such as:
     * viewing and updating patient records, managing appointments, setting availability, and changing password.
     *
     * @param hms The Hospital Management System (HMS) instance used to access hospital services.
     */
    @Override
    public void displayMenu(HMS hms) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Doctor Menu:");
            System.out.println("1. View Patient Medical Records");
            System.out.println("2. Update Patient Medical Records");
            System.out.println("3. View Personal Schedule");
            System.out.println("4. Set Availability for Appointments");
            System.out.println("5. Accept or Decline Appointments");
            System.out.println("6. View Upcoming Appointments");
            System.out.println("7. Record Appointment Outcome");
            System.out.println("8. Change Password");
            System.out.println("9. Logout");

            int choice = -1;

            // Validate input to ensure it's an integer within the valid range (1-9)
            while (true) {
                System.out.print("Enter your choice (1-9): ");

                // Check if input is an integer
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();  // Consume newline

                    // Check if choice is in the valid range
                    if (choice >= 1 && choice <= 9) {
                        break;  // Valid input
                    } else {
                        System.out.println("Invalid choice. Please enter a number between 1 and 9.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.next();  // Clear invalid input
                }
            }

            // Execute the selected menu option
            try {
                switch (choice) {
                    case 1:
                        hms.viewPatientRecords();  // View patient records
                        break;
                    case 2:
                        hms.updatePatientRecords();  // Update patient records
                        break;
                    case 3:
                        hms.viewDoctorSchedule(this);  // View doctor's personal schedule
                        break;
                    case 4:
                        hms.setDoctorAvailability(this);  // Set availability for appointments
                        break;
                    case 5:
                        hms.manageAppointmentRequests(this);  // Manage appointment requests
                        break;
                    case 6:
                        hms.viewUpcomingAppointments(this);  // View upcoming appointments
                        break;
                    case 7:
                        hms.recordAppointmentOutcome(this);  // Record outcome of an appointment
                        break;
                    case 8:
                        hms.changePassword(this, scanner);  // Change password
                        break;
                    case 9:
                        System.out.println("Logging out...");
                        return;  // Logout
                    default:
                        System.out.println("Unexpected error.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
