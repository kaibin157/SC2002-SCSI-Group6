package oop;

import oop.controller.AppointmentController;
import oop.controller.AuthenticationController;
import oop.controller.InventoryController;
import oop.controller.UserController;
import oop.model.*;
import oop.util.Constant;
import oop.util.Helper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.util.InputMismatchException;

/**
 * The {@code HMS} class represents the main controller for the Hospital Management System.
 * It handles user authentication, staff management, appointment scheduling,
 * inventory management, and various other hospital operations.
 */
public class HMS {
    private AuthenticationController authController;
    private InventoryController inventoryController;
    private AppointmentController appointmentController;
    private UserController userController;

    private List<ReplenishmentRequest> replenishmentRequests = new ArrayList<>();

    /**
     * Constructs a new {@code HMS} instance by initializing controllers and loading authentication data.
     *
     * @param authFilePath the file path to the authentication data file
     * @throws IOException if there is an error loading authentication data
     */
    public HMS(String authFilePath) throws IOException {
        this.authController = new AuthenticationController(authFilePath);  // Load authentication data
        userController = new UserController();
        inventoryController = new InventoryController();
        appointmentController = new AppointmentController();
    }

    /**
     * Initializes the system by loading staff, patients, and medication data from Excel files.
     * This method should be called after constructing the {@code HMS} instance to ensure all data is loaded.
     */
    public void initializeSystem() {
        try {
            userController.loadStaffFromExcel();
            userController.loadPatientsFromExcel();
            inventoryController.loadMedicationsFromExcel();
            this.replenishmentRequests = new ArrayList<>();

        } catch (IOException e) {
            System.out.println("Error reading Excel files: " + e.getMessage());
        }
    }

    /**
     * Handles user login authentication.
     * If the user's password is the default, it forces the user to change the password.
     *
     * @param hospitalID the hospital ID of the user attempting to log in
     * @param password   the password provided by the user
     * @param scanner    a {@link Scanner} object for reading user input
     * @return the authenticated {@link User} object if login is successful; {@code null} otherwise
     * @throws IOException if an I/O error occurs during authentication
     */
    public User login(String hospitalID, String password, Scanner scanner) throws IOException {
        var users = userController.getUsers();
        // Authenticate user
        if (authController.authenticate(hospitalID, password)) {
            // Find and return the user if authentication succeeds
            for (User user : users) {
                if (user.getHospitalID().equals(hospitalID)) {
                    // Check if the password is the default "password"
                    if (password.equals("password")) {
                        System.out.println("You are logging in for the first time. You must change your password.");
                        // Force the user to change their password
                        forcePasswordChange(hospitalID, scanner);
                        return null;  // Force a re-login after password change, don't show invalid credentials message
                    }
                    return user;  // Return the user object on successful authentication
                }
            }
        }
        // If authentication fails, return null to signal invalid credentials
        return null;
    }

    /**
     * Forces the user to change their password upon first login or when required.
     *
     * @param hospitalID the hospital ID of the user whose password needs to be changed
     * @param scanner    a {@link Scanner} object for reading user input
     * @throws IOException if an I/O error occurs during password update
     */
    private void forcePasswordChange(String hospitalID, Scanner scanner) throws IOException {
        while (true) {
            System.out.println("Enter a new password (must be at least 12 characters long and include uppercase letters, lowercase letters, numbers, and symbols):");
            String newPassword = scanner.nextLine();

            // Validate the password strength
            if (!Helper.isValidPassword(newPassword)) {
                System.out.println("Password does not meet the security requirements. Please try again.");
                continue;
            }

            System.out.println("Confirm your new password:");
            String confirmPassword = scanner.nextLine();

            // Check if the passwords match
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("Passwords do not match. Please try again.");
                continue;
            }

            // If valid and matching, update the password
            authController.updatePassword(hospitalID, newPassword);
            System.out.println("Password changed successfully. Please log in again with your new password.");
            break;
        }
    }

    /**
     * Allows the administrator to manage hospital staff, including adding, updating, and removing staff members.
     * Displays the current staff list and prompts the administrator for an action.
     */
    public void manageHospitalStaff() {
        Scanner scanner = new Scanner(System.in);

        try {
            // Show the list of hospital staff before the user picks an action
            userController.viewHospitalStaff();

            int choice = -1;
            // Input validation loop to ensure the user can only input 1, 2, or 3
            System.out.println("1. Add Staff");
            System.out.println("2. Update Staff");
            System.out.println("3. Remove Staff");
            System.out.println("Please choose an option (1, 2, or 3):");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline
                if (choice < 1 || choice > 3) {
                    System.out.println("Invalid choice. Returning to the main menu.");
                    return;  // Return to main menu after invalid input
                }
            } else {
                System.out.println("Invalid input.");
                scanner.next();  // Consume invalid input
                return;  // Return to main menu after invalid input
            }

            // Handle the valid choice
            switch (choice) {
                case 1:
                    // Add new staff
                    userController.addStaffMember(scanner);
                    break;
                case 2:
                    // Update existing staff
                    userController.updateStaffMember(scanner);
                    break;
                case 3:
                    // Remove a staff member
                    userController.removeStaffMember(scanner);
                    break;
                default:
                    // This block will never be reached due to validation
                    System.out.println("Invalid choice.");
            }
        } catch (IOException e) {
            System.out.println("Error managing staff: " + e.getMessage());
        }
    }

    /**
     * Views appointment details by delegating to the {@link AppointmentController}.
     *
     * @throws IOException if an I/O error occurs while retrieving appointment details
     */
    public void viewAppointmentDetails() throws IOException {
        appointmentController.viewAppointmentDetails();
    }

    /**
     * Manages the medication inventory by displaying current inventory and allowing the administrator
     * to add, update, or remove medications.
     *
     * @param scanner a {@link Scanner} object for reading user input
     * @throws IOException if an I/O error occurs while accessing the medication data
     */
    public void manageMedicationInventory(Scanner scanner) throws IOException {
        // Open the Excel file to read medication data
        FileInputStream file = new FileInputStream(Constant.MEDICINE_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        // Display the list of medications
        System.out.println("Medication Inventory:");
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String medicationName = row.getCell(0).getStringCellValue();
            int stockLevel = (int) row.getCell(1).getNumericCellValue();
            int lowStockAlert = (int) row.getCell(2).getNumericCellValue();

            System.out.println("Medication: " + medicationName + " | Stock Level: " + stockLevel + " | Low Stock Alert: " + lowStockAlert);
        }

        boolean validInput = false;  // Flag to check if input is valid
        while (!validInput) {
            try {
                // Ask the administrator to choose an action
                System.out.println("\nWhat would you like to do?");
                System.out.println("1. Add Medication");
                System.out.println("2. Update Medication Stock");
                System.out.println("3. Remove Medication");

                int choice = scanner.nextInt();  // Attempt to read an integer
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 1:
                        inventoryController.addMedication(scanner, sheet, workbook);
                        validInput = true;
                        break;
                    case 2:
                        inventoryController.updateMedicationStock(scanner, sheet, workbook);
                        validInput = true;
                        break;
                    case 3:
                        inventoryController.removeMedication(scanner, sheet, workbook);
                        validInput = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 3.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine();  // Clear the invalid input
            }
        }

        // Close the workbook and file
        workbook.close();
        file.close();
    }

    /**
     * Views patient records (doctors can only see their patients' records).
     *
     * @throws IOException if an I/O error occurs while accessing patient records
     */
    public void viewPatientRecords() throws IOException {
        userController.viewPatientRecords();
    }

    /**
     * Updates patient records by delegating to the {@link UserController}.
     *
     * @throws IOException if an I/O error occurs while updating patient records
     */
    public void updatePatientRecords() throws IOException {
        userController.updatePatientRecords();
    }

    /**
     * Views the doctor's personal schedule (appointments).
     *
     * @param doctor the {@link Doctor} whose schedule is to be viewed
     * @throws IOException if an I/O error occurs while accessing the schedule
     */
    public void viewDoctorSchedule(Doctor doctor) throws IOException {
        var appointments = appointmentController.getAppointments();
        userController.viewDoctorSchedule(doctor, appointments);
    }

    /**
     * Sets the doctor's availability for appointments.
     *
     * @param doctor the {@link Doctor} whose availability is to be set
     * @throws IOException if an I/O error occurs while updating availability
     */
    public void setDoctorAvailability(Doctor doctor) throws IOException {
        userController.setDoctorAvailability(doctor);
    }

    /**
     * Manages appointment requests by allowing the doctor to accept or decline pending appointments.
     *
     * @param doctor the {@link Doctor} who will manage the appointments
     * @throws IOException if an I/O error occurs while managing appointments
     */
    public void manageAppointmentRequests(Doctor doctor) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Open the Excel file to read the appointment list
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        boolean hasPendingAppointments = false;  // Flag to check if pending appointments are found

        // Loop through the rows in the sheet to find pending appointments for this doctor
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            // Check if the doctor ID in the Excel file matches the logged-in doctor's ID and the status is "pending"
            String doctorIDInExcel = row.getCell(0).getStringCellValue();  // Doctor ID (Column 2 in the Excel file)
            String statusInExcel = row.getCell(6).getStringCellValue();    // Status (Column 7 in the Excel file)

            if (doctorIDInExcel.equalsIgnoreCase(doctor.getHospitalID()) && statusInExcel.equalsIgnoreCase("pending")) {
                hasPendingAppointments = true;

                // Get Appointment details from the respective columns
                String appointmentID = row.getCell(2).getStringCellValue();  // Appointment ID (Column 3)
                String patientName = row.getCell(4).getStringCellValue();    // Patient Name (Column 5)
                String dateTime = row.getCell(5).getStringCellValue();       // Date/Time (Column 6)

                // Display the pending appointment details
                System.out.println("Appointment ID: " + appointmentID);
                System.out.println("Patient: " + patientName);
                System.out.println("Date/Time: " + dateTime);

                // Ask the doctor to accept or decline the appointment
                System.out.println("Do you want to accept this appointment? (yes/no)");
                String decision = scanner.nextLine();

                // Update the status based on doctor's decision
                if (decision.equalsIgnoreCase("yes")) {
                    row.getCell(6).setCellValue("confirmed");  // Update status to "confirmed" in Excel (Column 7)
                    System.out.println("Appointment confirmed.");
                } else {
                    row.getCell(6).setCellValue("cancelled");  // Update status to "cancelled" in Excel (Column 7)
                    System.out.println("Appointment declined.");
                }
            }
        }

        // If no pending appointments are found
        if (!hasPendingAppointments) {
            System.out.println("No pending appointment requests found.");
        }

        // Write the updated workbook back to the Excel file
        FileOutputStream outputStream = new FileOutputStream(Constant.APPOINTMENT_FILE_PATH);
        workbook.write(outputStream);

        // Close all resources
        workbook.close();
        file.close();
        outputStream.close();
    }

    /**
     * Views available appointment slots by delegating to the {@link AppointmentController}.
     *
     * @throws IOException if an I/O error occurs while accessing appointment slots
     */
    public void viewAvailableAppointmentSlots() throws IOException {
        appointmentController.viewAvailableAppointmentSlots();
    }

    /**
     * Schedules an appointment for a patient.
     *
     * @param patient the {@link Patient} for whom the appointment is being scheduled
     * @throws IOException if an I/O error occurs while scheduling the appointment
     */
    public void scheduleAppointment(Patient patient) throws IOException {
        var doctors = userController.getDoctors();
        appointmentController.scheduleAppointment(patient, doctors);
    }

    /**
     * Reschedules an existing appointment for a patient.
     *
     * @param patient the {@link Patient} whose appointment is being rescheduled
     * @throws IOException if an I/O error occurs while rescheduling the appointment
     */
    public void rescheduleAppointment(Patient patient) throws IOException {
        appointmentController.rescheduleAppointment(patient);
    }

    /**
     * Cancels an appointment for a patient.
     *
     * @param patient the {@link Patient} whose appointment is being cancelled
     * @throws IOException if an I/O error occurs while cancelling the appointment
     */
    public void cancelAppointment(Patient patient) throws IOException {
        appointmentController.cancelAppointment(patient);
    }

    /**
     * Views scheduled appointments for a patient.
     *
     * @param patient the {@link Patient} whose scheduled appointments are to be viewed
     * @throws IOException if an I/O error occurs while accessing scheduled appointments
     */
    public void viewScheduledAppointments(Patient patient) throws IOException {
        appointmentController.viewScheduledAppointments(patient);
    }

    /**
     * Views appointment outcome records by delegating to the {@link AppointmentController}.
     *
     * @throws IOException if an I/O error occurs while accessing appointment outcome records
     */
    public void viewAppointmentOutcomeRecord() throws IOException {
        appointmentController.viewAppointmentOutcomeRecord();
    }

    /**
     * Updates the prescription status, such as changing it from "pending" to "dispensed".
     *
     * @throws IOException if an I/O error occurs while updating prescription status
     */
    public void updatePrescriptionStatus() throws IOException {
        inventoryController.updatePrescriptionStatus();
    }

    /**
     * Views the medication inventory for pharmacists.
     *
     * @throws IOException if an I/O error occurs while accessing the medication inventory
     */
    public void viewMedicationInventory() throws IOException {
        inventoryController.viewMedicationInventory();
    }

    /**
     * Views upcoming appointments for a doctor.
     *
     * @param doctor the {@link Doctor} whose upcoming appointments are to be viewed
     * @throws IOException if an I/O error occurs while accessing appointments
     */
    public void viewUpcomingAppointments(Doctor doctor) throws IOException {
        appointmentController.viewUpcomingAppointments(doctor);
    }

    /**
     * Records the outcome of an appointment.
     *
     * @param doctor the {@link Doctor} who is recording the appointment outcome
     * @throws IOException if an I/O error occurs while recording the outcome
     */
    public void recordAppointmentOutcome(Doctor doctor) throws IOException {
        appointmentController.recordAppointmentOutcome(doctor);
    }

    /**
     * Views past appointment outcomes for a patient.
     *
     * @param patient the {@link Patient} whose past appointment outcomes are to be viewed
     * @throws IOException if an I/O error occurs while accessing appointment outcomes
     */
    public void viewPastAppointmentOutcomes(Patient patient) throws IOException {
        appointmentController.viewPastAppointmentOutcomes(patient);
    }

    /**
     * Allows a user to change their password.
     *
     * @param user    the {@link User} who wants to change their password
     * @param scanner a {@link Scanner} object for reading user input
     * @throws IOException if an I/O error occurs during password update
     */
    public void changePassword(User user, Scanner scanner) throws IOException {
        while (true) {
            // Prompt for current password and validate
            System.out.println("Enter your current password:");
            String currentPassword = scanner.nextLine();

            // Verify current password
            if (!authController.authenticate(user.getHospitalID(), currentPassword)) {
                System.out.println("Current password is incorrect. Please try again.");
                continue;
            }

            // Prompt for new password
            System.out.println("Enter a new password (must be at least 12 characters long and include uppercase letters, lowercase letters, numbers, and symbols):");
            String newPassword = scanner.nextLine();

            // Prompt for confirmation password
            System.out.println("Confirm your new password:");
            String confirmPassword = scanner.nextLine();

            // Validate the new password
            if (!Helper.isValidPassword(newPassword)) {
                System.out.println("Password does not meet the security requirements. Please try again.");
                continue;
            }

            // Check if new password matches confirmation
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("Passwords do not match. Please try again.");
                continue;
            }

            // Update the password in the Authentication_List.xlsx file
            authController.updatePassword(user.getHospitalID(), newPassword);
            System.out.println("Password changed successfully.");
            break;
        }
    }

    /**
     * Submits a replenishment request for medication that is low in stock.
     *
     * @param pharmacist the {@link Pharmacist} submitting the request
     * @throws IOException if an I/O error occurs while submitting the request
     */
    public void submitReplenishmentRequest(Pharmacist pharmacist) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Reload medication inventory from Excel to ensure we have the latest stock levels
        FileInputStream file = new FileInputStream(Constant.MEDICINE_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        System.out.println("Medication Inventory:");

        List<Row> lowStockMedicines = new ArrayList<>();

        // Display only medications with stock levels below the low stock alert level
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String medicationName = row.getCell(0).getStringCellValue();
            int stockLevel = (int) row.getCell(1).getNumericCellValue();
            int lowStockAlert = (int) row.getCell(2).getNumericCellValue();

            // Only show medications with stock levels below the low stock alert level
            if (stockLevel < lowStockAlert) {
                lowStockMedicines.add(row);  // Collect the rows that need replenishment
                System.out.println(lowStockMedicines.size() + ". Medication: " + medicationName + " | Stock Level: " + stockLevel + " | Low Stock Alert: " + lowStockAlert);
            }
        }

        // If there are no medications that need replenishment
        if (lowStockMedicines.isEmpty()) {
            System.out.println("All medications are sufficiently stocked. No replenishment requests needed.");
            workbook.close();
            file.close();
            return;
        }

        // Ask the pharmacist to select a medication for replenishment
        System.out.println("\nEnter the number of the medication to request replenishment:");
        int medicationChoice = -1;
        while (true) {
            if (scanner.hasNextInt()) {
                medicationChoice = scanner.nextInt();
                scanner.nextLine();  // Consume newline
                if (medicationChoice > 0 && medicationChoice <= lowStockMedicines.size()) {
                    break;  // Valid choice
                } else {
                    System.out.println("Please enter a valid number between 1 and " + lowStockMedicines.size());
                }
            } else {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next();  // Clear invalid input
            }
        }

        // Get the selected medication row
        Row selectedRow = lowStockMedicines.get(medicationChoice - 1);
        String selectedMedicationName = selectedRow.getCell(0).getStringCellValue();

        // Request replenishment amount
        System.out.println("Enter the amount to request for replenishment:");
        int replenishmentAmount = -1;
        while (true) {
            if (scanner.hasNextInt()) {
                replenishmentAmount = scanner.nextInt();
                scanner.nextLine();  // Consume newline
                if (replenishmentAmount > 0) {
                    break;
                } else {
                    System.out.println("Please enter a positive amount.");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next();  // Clear invalid input
            }
        }

        // Write the replenishment request to Replenishment_List.xlsx
        appendReplenishmentToExcel(pharmacist.getHospitalID(), selectedMedicationName, replenishmentAmount);

        System.out.println("Replenishment request submitted successfully and is pending approval.");

        workbook.close();
        file.close();
    }

    /**
     * Appends a replenishment request to the Excel file.
     *
     * @param pharmacistID       the hospital ID of the pharmacist submitting the request
     * @param medicationName     the name of the medication to replenish
     * @param replenishmentAmount the amount of medication to request
     * @throws IOException if an I/O error occurs while writing to the Excel file
     */
    private void appendReplenishmentToExcel(String pharmacistID, String medicationName, int replenishmentAmount) throws IOException {
        // Open the Replenishment_List.xlsx file
        FileInputStream file = new FileInputStream(Constant.REPLENISHMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        // Find the last row in the Replenishment_List.xlsx file
        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);

        // Write the replenishment request details to the new row
        newRow.createCell(0).setCellValue(pharmacistID);  // Pharmacist ID
        newRow.createCell(1).setCellValue(medicationName);  // Medication
        newRow.createCell(2).setCellValue(replenishmentAmount);  // Requested Amount
        newRow.createCell(3).setCellValue("pending");  // Status defaults to "pending"

        // Write the updated workbook back to the Excel file
        FileOutputStream outputStream = new FileOutputStream(Constant.REPLENISHMENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        file.close();
    }

    /**
     * Approves pending replenishment requests and updates the medication stock accordingly.
     *
     * <p>This method performs the following actions:
     * <ul>
     *   <li>Displays all pending replenishment requests.</li>
     *   <li>Allows the administrator to select a request to approve.</li>
     *   <li>Updates the status of the selected request to "approved".</li>
     *   <li>Updates the medication stock in the inventory based on the approved request.</li>
     * </ul>
     *
     * @throws IOException if an I/O error occurs while accessing the Excel files
     */
    public void approveReplenishmentRequests() throws IOException {
        // Open the Replenishment_List.xlsx file
        FileInputStream replenishmentFile = new FileInputStream(Constant.REPLENISHMENT_FILE_PATH);
        Workbook replenishmentWorkbook = new XSSFWorkbook(replenishmentFile);
        Sheet replenishmentSheet = replenishmentWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

        List<Row> pendingRequests = new ArrayList<>();

        // List all pending requests
        System.out.println("Pending Replenishment Requests:");
        boolean hasPendingRequests = false;
        int index = 1;

        for (Row row : replenishmentSheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String status = Helper.getCellValueAsString(row.getCell(3));  // Assuming Status is in column 4

            // Only show pending requests
            if (status.equalsIgnoreCase("pending")) {
                hasPendingRequests = true;
                pendingRequests.add(row);
                String pharmacistID = Helper.getCellValueAsString(row.getCell(0));  // Pharmacist ID (Column 1)
                String medicationName = Helper.getCellValueAsString(row.getCell(1));  // Medication Name (Column 2)
                String requestedAmount = Helper.getCellValueAsString(row.getCell(2));  // Requested Amount (Column 3)

                System.out.println(index + ". Pharmacist ID: " + pharmacistID + " | Medication: " + medicationName + " | Requested Amount: " + requestedAmount + " | Status: " + status);
                index++;
            }
        }

        // If no pending requests
        if (!hasPendingRequests) {
            System.out.println("No pending replenishment requests found.");
            replenishmentWorkbook.close();
            replenishmentFile.close();
            return;
        }

        // Select request to approve
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of the request to approve: ");

        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Returning to the main menu.");
            replenishmentWorkbook.close();
            replenishmentFile.close();
            return;
        }

        int requestNumber = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        // Validate request number
        if (requestNumber < 1 || requestNumber > pendingRequests.size()) {
            System.out.println("Invalid selection.");
            replenishmentWorkbook.close();
            replenishmentFile.close();
            return;
        }

        // Get the selected request row
        Row selectedRow = pendingRequests.get(requestNumber - 1);
        String medicationName = Helper.getCellValueAsString(selectedRow.getCell(1));  // Medication Name (Column 2)
        int requestedAmount = Integer.parseInt(Helper.getCellValueAsString(selectedRow.getCell(2)));  // Requested Amount (Column 3)

        // Approve the selected request (Update the status in column 4)
        Helper.createOrUpdateCell(selectedRow, 3, "approved");  // Update Status to "approved"
        System.out.println("Replenishment request approved successfully.");

        // Write the updated status back to the Replenishment_List.xlsx file
        replenishmentFile.close();  // Close input stream
        FileOutputStream replenishmentOutputStream = new FileOutputStream(Constant.REPLENISHMENT_FILE_PATH);
        replenishmentWorkbook.write(replenishmentOutputStream);
        replenishmentWorkbook.close();
        replenishmentOutputStream.close();

        // Now update the Initial Stock in Medicine_List.xlsx
        inventoryController.updateMedicineStock(medicationName, requestedAmount);
    }

    /**
     * Updates patient information in the Excel file.
     *
     * @param patient the {@link Patient} whose information needs to be updated
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void updatePatientInfoInExcel(Patient patient) throws IOException {
        userController.updatePatientInfoInExcel(patient);
    }

    /**
     * Allows a pharmacist to view their replenishment requests and their statuses.
     *
     * @param pharmacist the {@link Pharmacist} whose requests are to be viewed
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void viewReplenishmentRequests(Pharmacist pharmacist) throws IOException {
        // Open the Replenishment_List.xlsx file
        FileInputStream file = new FileInputStream(Constant.REPLENISHMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        boolean hasRequests = false;

        // Loop through each row and display the details of each replenishment request
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String pharmacistID = Helper.getCellValueAsString(row.getCell(0));  // Pharmacist ID (Column 1)
            String medicationName = Helper.getCellValueAsString(row.getCell(1));  // Medication Name (Column 2)
            String requestedAmount = Helper.getCellValueAsString(row.getCell(2));  // Requested Amount (Column 3)
            String status = Helper.getCellValueAsString(row.getCell(3));  // Status (Column 4)

            // Check if the pharmacist ID matches the current logged-in pharmacist
            if (pharmacist.getHospitalID().equalsIgnoreCase(pharmacistID)) {
                // Display the replenishment request details
                System.out.println("Medication: " + medicationName + " | Requested Amount: " + requestedAmount + " | Status: " + status);
                hasRequests = true;
            }
        }

        if (!hasRequests) {
            System.out.println("No replenishment requests");
        }

        workbook.close();
        file.close();
    }

    /**
     * Handles outstanding bills for a patient by allowing them to view and pay unpaid appointments.
     *
     * @param patient the {@link Patient} whose outstanding bills are to be handled
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void handleOutstandingBills(Patient patient) throws IOException {
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        List<Row> outstandingAppointments = new ArrayList<>();
        System.out.println("Outstanding Bills for " + patient.getName() + ":");

        // Loop through the rows to find unpaid appointments
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String patientID = Helper.getCellValueAsString(row.getCell(1));  // Assuming patient ID is in column 2
            String status = Helper.getCellValueAsString(row.getCell(6));  // Assuming status is in column 7

            // If the patient ID matches and the appointment is unpaid
            if (patient.getHospitalID().equalsIgnoreCase(patientID) && status.equalsIgnoreCase("unpaid")) {
                outstandingAppointments.add(row);
                String appointmentID = Helper.getCellValueAsString(row.getCell(2));  // Assuming appointment ID is in column 3
                String docName = Helper.getCellValueAsString(row.getCell(3));
                String date = Helper.getCellValueAsString(row.getCell(5));  // Assuming date/time is in column 6
                double price = row.getCell(11).getNumericCellValue();
                System.out.println((outstandingAppointments.size()) + ". Appointment ID: " + appointmentID + " | Doctor: " + docName + " | Date/Time: " + date + " | Total: $" + price);
            }
        }

        if (outstandingAppointments.isEmpty()) {
            System.out.println("You have no outstanding bills.");
            workbook.close();
            file.close();
            return;
        }

        // Ask the user to choose which appointment they want to pay for
        Scanner scanner = new Scanner(System.in);
        int choice = -1;
        while (true) {
            System.out.print("Enter the number of the appointment you want to pay for: ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline
                if (choice >= 1 && choice <= outstandingAppointments.size()) {
                    break;  // Valid choice
                } else {
                    System.out.println("Invalid choice. Please enter a valid number.");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next();  // Clear invalid input
            }
        }

        // Proceed with payment
        Row selectedRow = outstandingAppointments.get(choice - 1);
        Payment payment = new Payment();
        payment.processPayment(selectedRow, patient, scanner);

        // Update the appointment status to "paid"
        selectedRow.getCell(6).setCellValue("paid");

        // Write changes back to the Excel file
        file.close();
        FileOutputStream outputStream = new FileOutputStream(Constant.APPOINTMENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    /**
     * Sends an invoice to patients by changing the status of completed appointments to "unpaid"
     * and adding the invoice amount.
     *
     * @param pharmacist the {@link Pharmacist} who is sending the invoice
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void sendInvoice(Pharmacist pharmacist) throws IOException {
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        List<Row> completedAppointments = new ArrayList<>();
        System.out.println("Completed Appointments:");

        // Loop through the rows to find completed appointments
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String status = Helper.getCellValueAsString(row.getCell(6));  // Assuming status is in column 7

            // If the appointment is completed
            if (status.equalsIgnoreCase("completed")) {
                completedAppointments.add(row);
                String appointmentID = Helper.getCellValueAsString(row.getCell(2));  // Assuming appointment ID is in column 3
                String patientName = Helper.getCellValueAsString(row.getCell(4));    // Assuming patient name is in column 5
                String date = Helper.getCellValueAsString(row.getCell(5));           // Assuming date/time is in column 6

                System.out.println((completedAppointments.size()) + ". Appointment ID: " + appointmentID + " | Patient: " + patientName + " | Date/Time: " + date);
            }
        }

        if (completedAppointments.isEmpty()) {
            System.out.println("There are no completed appointments.");
            workbook.close();
            file.close();
            return;
        }

        // Ask the pharmacist to choose which appointment to send an invoice for
        Scanner scanner = new Scanner(System.in);
        int choice = -1;
        while (true) {
            System.out.print("Enter the number of the appointment to send an invoice for: ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline
                if (choice >= 1 && choice <= completedAppointments.size()) {
                    break;  // Valid choice
                } else {
                    System.out.println("Invalid choice. Please enter a valid number.");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next();  // Clear invalid input
            }
        }

        // Ask the pharmacist to enter the price
        double price = 0.0;
        while (true) {
            System.out.print("Enter the amount: $");
            if (scanner.hasNextDouble()) {
                price = scanner.nextDouble();
                scanner.nextLine();  // Consume newline
                if (price > 0) {
                    break;  // Valid price
                } else {
                    System.out.println("Amount must be a positive number.");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid price.");
                scanner.next();  // Clear invalid input
            }
        }

        // Format the price to two decimal places
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedPrice = df.format(price);

        // Get the selected appointment and update its status to "unpaid"
        Row selectedRow = completedAppointments.get(choice - 1);
        selectedRow.getCell(6).setCellValue("unpaid");  // Change status to "unpaid"

        // Optionally, add a new column to store the invoice amount
        Cell priceCell = selectedRow.createCell(11);  // Assuming the price is to be saved in a new column 12 (index 11)
        priceCell.setCellValue(Double.parseDouble(formattedPrice));

        // Write changes back to the Excel file
        file.close();
        FileOutputStream outputStream = new FileOutputStream(Constant.APPOINTMENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();

        System.out.println("Invoice sent successfully.");
    }

    /**
     * Finds the last appointment ID by delegating to the {@link AppointmentController}.
     *
     * @return the last appointment ID as a {@code String}
     * @throws IOException if an I/O error occurs while accessing appointment data
     */
    public String findLastAppointmentID() throws IOException {
        return appointmentController.findLastAppointmentID();
    }

    /**
     * Allows a patient to view their medical records.
     *
     * @param patient the {@link Patient} whose medical records are to be viewed
     * @throws IOException if an I/O error occurs while accessing medical records
     */
    public void viewMedicalRecords(Patient patient) throws IOException {
        inventoryController.viewMedicalRecords(patient);
    }


}






	



