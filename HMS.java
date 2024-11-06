package oop;

import oop.controller.AppointmentController;
import oop.controller.AuthenticationController;
import oop.controller.InventoryController;
import oop.controller.UserController;
import oop.models.*;
import oop.utils.Constant;
import oop.utils.Helper;
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

public class HMS {
    private AuthenticationController authController;
    private InventoryController inventoryController;
    private AppointmentController appointmentController;
    private UserController userController;

    private List<ReplenishmentRequest> replenishmentRequests = new ArrayList<>();

    // Constructor to initialize the HMS system and load authentication data
    public HMS(String authFilePath) throws IOException {
        this.authController = new AuthenticationController(authFilePath);  // Load authentication data
        userController = new UserController();
        inventoryController = new InventoryController();
        appointmentController = new AppointmentController();
    }

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

    // Method to handle login authentication
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

    // 2. View Appointment Details
    public void viewAppointmentDetails() throws IOException {
        appointmentController.viewAppointmentDetails();
    }

    // 3. Manage Medication Inventory
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

    // Method for viewing patient records (doctor can only see their patients' records)
    public void viewPatientRecords() throws IOException {
        userController.viewPatientRecords();
    }

    // Method for updating patient records
    public void updatePatientRecords() throws IOException {
        userController.updatePatientRecords();
    }

    // Method for viewing doctor's personal schedule (appointments)
    public void viewDoctorSchedule(Doctor doctor) throws IOException {
        var appointments = appointmentController.getAppointments();
        userController.viewDoctorSchedule(doctor, appointments);
    }

    // Method for setting doctor's availability for appointments
    public void setDoctorAvailability(Doctor doctor) throws IOException {
        userController.setDoctorAvailability(doctor);
    }

    // Method for managing appointment requests (accept or decline)
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

    public void viewAvailableAppointmentSlots() throws IOException {
        appointmentController.viewAvailableAppointmentSlots();
    }

    // 2. Schedule an appointment for a patient
    public void scheduleAppointment(Patient patient) throws IOException {
        var doctors = userController.getDoctors();
        appointmentController.scheduleAppointment(patient, doctors);
    }

    // 3. Reschedule an appointment for a patient
    public void rescheduleAppointment(Patient patient) throws IOException {
        appointmentController.rescheduleAppointment(patient);
    }

    // 4. Cancel an appointment for a patient
    public void cancelAppointment(Patient patient) throws IOException {
        appointmentController.cancelAppointment(patient);
    }

    // 5. View scheduled appointments for a patient
    public void viewScheduledAppointments(Patient patient) throws IOException {
        appointmentController.viewScheduledAppointments(patient);
    }

    public void viewAppointmentOutcomeRecord() throws IOException {
        appointmentController.viewAppointmentOutcomeRecord();
    }

    // 2. Update Prescription Status (e.g., from "pending" to "dispensed")
    public void updatePrescriptionStatus() throws IOException {
        inventoryController.updatePrescriptionStatus();
    }

    // 3. View Medication Inventory for pharmacists
    public void viewMedicationInventory() throws IOException {
        inventoryController.viewMedicationInventory();
    }

    public void viewUpcomingAppointments(Doctor doctor) throws IOException {
        appointmentController.viewUpcomingAppointments(doctor);
    }

    public void recordAppointmentOutcome(Doctor doctor) throws IOException {
        appointmentController.recordAppointmentOutcome(doctor);
    }

    public void viewPastAppointmentOutcomes(Patient patient) throws IOException {
        appointmentController.viewPastAppointmentOutcomes(patient);
    }

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

    public void updatePatientInfoInExcel(Patient patient) throws IOException {
        userController.updatePatientInfoInExcel(patient);
    }

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

    public void handleOutstandingBills(Patient patient) throws IOException {
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        List<Row> outstandingAppointments = new ArrayList<>();
        System.out.println("Outstanding Bills for " + patient.getName() + ":");

        // Loop through the rows to find completed appointments
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String patientID = Helper.getCellValueAsString(row.getCell(1));  // Assuming patient ID is in column 2
            String status = Helper.getCellValueAsString(row.getCell(6));  // Assuming status is in column 7

            // If the patient ID matches and the appointment is completed
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

    public String findLastAppointmentID() throws IOException {
        return appointmentController.findLastAppointmentID();
    }

    public void viewMedicalRecords(Patient patient) throws IOException{
        inventoryController.viewMedicalRecords(patient);
    }

}






	



