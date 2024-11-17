package oop.model;

import oop.HMS;
import oop.util.Helper;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * The Patient class represents a patient in the healthcare management system.
 * It provides methods for updating personal information, managing appointments,
 * viewing medical records, and handling billing.
 */
public class Patient extends User {

    /** The patient's date of birth. */
    private String dateOfBirth;

    /** The patient's gender. */
    private String gender;

    /** The patient's email address. */
    private String email;

    /** The patient's phone number. */
    private String phoneNumber;

    /** The patient's blood type. */
    private String bloodType;

    /** A list of the patient's past diagnoses. */
    private List<String> pastDiagnoses;

    /**
     * Constructs a Patient object with the specified details.
     *
     * @param hospitalID   The hospital ID for the patient.
     * @param password     The patient's password.
     * @param name         The patient's name.
     * @param dateOfBirth  The patient's date of birth.
     * @param gender       The patient's gender.
     * @param email        The patient's email address.
     * @param phoneNumber  The patient's phone number.
     * @param bloodType    The patient's blood type.
     */
    public Patient(String hospitalID, String password, String name, String dateOfBirth, String gender,
                   String email, String phoneNumber, String bloodType) {
        super(hospitalID, password, name);  // Call the User constructor
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.bloodType = bloodType;
        this.pastDiagnoses = new ArrayList<>();  // Initialize as an empty list
    }

    // Getters and setters

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBloodType() {
        return bloodType;
    }

    public List<String> getPastDiagnoses() {
        return pastDiagnoses;
    }

    /**
     * Adds a diagnosis to the patient's list of past diagnoses.
     *
     * @param diagnosis The diagnosis to add.
     */
    public void addDiagnosis(String diagnosis) {
        this.pastDiagnoses.add(diagnosis);
    }

    /**
     * Displays the patient menu and handles user input to perform various actions.
     *
     * @param hms The HMS instance to access system methods.
     */
    @Override
    public void displayMenu(HMS hms) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Patient Menu:");
            System.out.println("1. View Medical Record");
            System.out.println("2. Update Personal Information");
            System.out.println("3. View Available Appointment Slots");
            System.out.println("4. Schedule an Appointment");
            System.out.println("5. Reschedule an Appointment");
            System.out.println("6. Cancel an Appointment");
            System.out.println("7. View Scheduled Appointments");
            System.out.println("8. View Past Appointment Outcome Records");
            System.out.println("9. Pay Outstanding Bills");
            System.out.println("10. Change Password");
            System.out.println("11. Logout");

            int choice = Helper.getChoice(scanner, 1, 11);
            if (choice == 11) {
            	System.out.println("Logging out...");
            	return;
            } else {
                executeChoice(choice, hms, scanner); // Handle other choices
            }
        }
    }

    /**
     * Updates the patient's personal information based on user input.
     *
     * @param scanner The scanner instance for user input.
     * @param hms     The HMS instance to update information in storage.
     */
    public void updatePersonalInfo(Scanner scanner, HMS hms) {
        while (true) {
            System.out.println("What would you like to update?");
            System.out.println("1. Email");
            System.out.println("2. Phone Number");
            System.out.print("Enter your choice (1 or 2): ");

            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                updateEmail(scanner);
                break;
            } else if (choice.equals("2")) {
                updatePhoneNumber(scanner);
                break;
            } else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
                return;
            }
        }

        try {
            hms.updatePatientInfoInExcel(this);  // Call HMS method to update Excel file
        } catch (IOException e) {
            System.out.println("Error updating patient information in Excel: " + e.getMessage());
        }
    }


    private void executeChoice(int choice, HMS hms, Scanner scanner) {
        try {
            switch (choice) {
                case 1 -> hms.viewMedicalRecords(this);
                case 2 -> updatePersonalInfo(scanner, hms);
                case 3 -> hms.viewAvailableAppointmentSlots();
                case 4 -> hms.scheduleAppointment(this);
                case 5 -> hms.rescheduleAppointment(this);
                case 6 -> hms.cancelAppointment(this);
                case 7 -> hms.viewScheduledAppointments(this);
                case 8 -> hms.viewPastAppointmentOutcomes(this);
                case 9 -> hms.handleOutstandingBills(this);
                case 10 -> hms.changePassword(this, scanner);
                default -> System.out.println("Unexpected error.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateEmail(Scanner scanner) {
        while (true) {
            System.out.print("Enter your new email address: ");
            String newEmail = scanner.nextLine();
            if (newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                this.setEmail(newEmail);
                System.out.println("Email updated successfully.");
                break;
            } else {
                System.out.println("Invalid email format. Please enter a valid email.");
            }
        }
    }

    private void updatePhoneNumber(Scanner scanner) {
        while (true) {
            System.out.print("Enter your new phone number (digits only): ");
            String newPhoneNumber = scanner.nextLine();
            if (newPhoneNumber.matches("\\d{5,20}")) {
                this.setPhoneNumber(newPhoneNumber);
                System.out.println("Phone number updated successfully.");
                break;
            } else {
                System.out.println("Invalid phone number. Please enter a valid phone number.");
            }
        }
    }
}
