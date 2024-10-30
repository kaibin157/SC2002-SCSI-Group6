package oop;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;

public class HMS {
	private AuthenticationController authController;
	private int nextAppointmentNumber;
	
    private List<User> users = new ArrayList<>();
    private List<Doctor> doctors = new ArrayList<>();
    private List<Pharmacist> pharmacists = new ArrayList<>();
    private List<Patient> patients = new ArrayList<>();
    private List<Medication> inventory = new ArrayList<>();
    private List<Appointment> appointments = new ArrayList<>();
    private List<ReplenishmentRequest> replenishmentRequests;

    private static final String STAFF_FILE_PATH = "Staff_List.xlsx";
    private static final String PATIENT_FILE_PATH = "Patient_List.xlsx";
    private static final String MEDICINE_FILE_PATH = "Medicine_List.xlsx";
    private static final String APPOINTMENT_FILE_PATH = "Appointment_List.xlsx";
    private static final String AUTHENTICATION_FILE_PATH = "Authentication_List.xlsx";
    private static final String DOC_AVAILABILITY_FILE_PATH = "DocAvailability_List.xlsx";
    private static final String REPLENISHMENT_FILE_PATH = "Replenishment_List.xlsx";

    public HMS() {
        replenishmentRequests = new ArrayList<>();;
    }
    
    // Constructor to initialize the HMS system and load authentication data
    public HMS(String authFilePath) throws IOException {
        this.authController = new AuthenticationController(authFilePath);  // Load authentication data
        this.users = new ArrayList<>();
        this.patients = new ArrayList<>();
        this.inventory = new ArrayList<>();
        this.replenishmentRequests = new ArrayList<>(); 
    }
    
    public void initializeSystem() {
        try {
            loadStaffFromExcel();
            loadPatientsFromExcel();
            loadMedicationsFromExcel();

            String lastAppointmentID = findLastAppointmentID();
            nextAppointmentNumber = Integer.parseInt(lastAppointmentID.substring(3)) + 1;  // Get the number and increment it
        } catch (IOException e) {
            System.out.println("Error reading Excel files: " + e.getMessage());
        }
    }

    // Method to handle login authentication
    public User login(String hospitalID, String password, Scanner scanner) throws IOException {
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
            if (!isValidPassword(newPassword)) {
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

    
    private boolean isValidPassword(String password) {
        if (password.length() < 12) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }









    public void manageHospitalStaff() {
        Scanner scanner = new Scanner(System.in);

        try {
            // Show the list of hospital staff before the user picks an action
            viewHospitalStaff();

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
                    addStaffMember(scanner);
                    break;
                case 2:
                    // Update existing staff
                    updateStaffMember(scanner);
                    break;
                case 3:
                    // Remove a staff member
                    removeStaffMember(scanner);
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
        // Open the Appointment_List.xlsx file
        FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains appointment data

        System.out.println("Appointment Details:");

        // Loop through each row in the sheet
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            // Retrieve the necessary details from the respective columns
            String patientID = getCellValueAsString(row.getCell(1));  // Assuming Patient ID is in column 2
            String doctorID = getCellValueAsString(row.getCell(0));  // Assuming Doctor ID is in column 1
            String status = getCellValueAsString(row.getCell(6));    // Assuming Status is in column 7
            String appointmentDate = getCellValueAsString(row.getCell(5));  // Assuming Date is in column 6

            // Display the details
            System.out.println("Doctor ID: " + doctorID);
            System.out.println("Patient ID: " + patientID);
            System.out.println("Date: " + appointmentDate);
            System.out.println("Status: " + status);
            System.out.println("------------------------------------");
        }

        // Close the workbook and file
        workbook.close();
        file.close();
    }



    // Mocked method to get all appointments
    private List<Appointment> getAllAppointments() {
        // Assuming you would have a way to store and manage all appointments in the system
        return new ArrayList<>();  // Replace with actual appointment data
    }

    // 3. Manage Medication Inventory
    public void manageMedicationInventory(Scanner scanner) throws IOException {
        // Open the Excel file to read medication data
        FileInputStream file = new FileInputStream(MEDICINE_FILE_PATH);
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
                        addMedication(scanner, sheet, workbook);
                        validInput = true;
                        break;
                    case 2:
                        updateMedicationStock(scanner, sheet, workbook);
                        validInput = true;
                        break;
                    case 3:
                        removeMedication(scanner, sheet, workbook);
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

    
    public void addMedication(Scanner scanner, Sheet sheet, Workbook workbook) throws IOException {
        System.out.println("Enter the name of the new medication:");
        String medicationName = scanner.nextLine();

        System.out.println("Enter the stock level:");
        int stockLevel = scanner.nextInt();

        System.out.println("Enter the low stock alert level:");
        int lowStockAlert = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        // Find the next available row in the sheet
        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);

        // Write the medication details to the new row
        newRow.createCell(0).setCellValue(medicationName);
        newRow.createCell(1).setCellValue(stockLevel);
        newRow.createCell(2).setCellValue(lowStockAlert);

        // Save the changes to the Excel file
        FileOutputStream outputStream = new FileOutputStream(MEDICINE_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();

        System.out.println("Medication added successfully.");
    }




    // Update stock for a specific medication
    public void updateMedicationStock(Scanner scanner, Sheet sheet, Workbook workbook) throws IOException {
        System.out.println("Enter the name of the medication to update:");
        String medicationName = scanner.nextLine();

        boolean medicationFound = false;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            Cell nameCell = row.getCell(0);
            if (nameCell.getStringCellValue().equalsIgnoreCase(medicationName)) {
                System.out.println("Enter the new stock level for " + medicationName + ": ");
                int newStockLevel = scanner.nextInt();
                row.getCell(1).setCellValue(newStockLevel);  // Update stock level

                medicationFound = true;
                break;
            }
        }

        if (!medicationFound) {
            System.out.println("Medication not found in the inventory.");
        } else {
            // Write the updated data back to the Excel file
            FileOutputStream outputStream = new FileOutputStream(MEDICINE_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            System.out.println("Stock level updated successfully.");
        }
    }
    
    public void removeMedication(Scanner scanner, Sheet sheet, Workbook workbook) throws IOException {
        System.out.println("Enter the name of the medication to remove:");
        String medicationName = scanner.nextLine();

        boolean medicationFound = false;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {  // Start from row 1 to skip the header
            Row row = sheet.getRow(i);
            if (row == null) continue;  // Skip empty rows

            Cell nameCell = row.getCell(0);
            if (nameCell.getStringCellValue().equalsIgnoreCase(medicationName)) {
                // Remove the row by shifting the remaining rows up
                sheet.removeRow(row);
                medicationFound = true;
                break;
            }
        }

        if (!medicationFound) {
            System.out.println("Medication not found in the inventory.");
        } else {
            // Write the updated data back to the Excel file
            FileOutputStream outputStream = new FileOutputStream(MEDICINE_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            System.out.println("Medication removed successfully.");
        }
    }



    // Save the medication inventory back to Excel
 // Save the updated medication inventory back to the Excel file
    public void saveMedicationsToExcel() throws IOException {
        FileInputStream file = new FileInputStream(MEDICINE_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        // Loop through the medication list and update stock levels in the Excel sheet
        for (int i = 0; i < inventory.size(); i++) {
            Medication medication = inventory.get(i);
            Row row = sheet.getRow(i + 1);  // Assuming row 0 is the header
            if (row == null) {
                row = sheet.createRow(i + 1);
            }

            row.getCell(0).setCellValue(medication.getName());
            row.getCell(1).setCellValue(medication.getStockLevel());
            row.getCell(2).setCellValue(medication.getLowStockAlert());
        }

        FileOutputStream outFile = new FileOutputStream(MEDICINE_FILE_PATH);
        workbook.write(outFile);
        workbook.close();
        outFile.close();
        file.close();
    }

    

    public void loadStaffFromExcel() throws IOException {
        FileInputStream file = new FileInputStream(STAFF_FILE_PATH);  // Path to your staff Excel file
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip the header row

            // Map Excel columns to variables
            String hospitalID = getCellValueAsString(row.getCell(0));  // Staff ID (Column 1)
            String name = getCellValueAsString(row.getCell(1));        // Name (Column 2)
            String role = getCellValueAsString(row.getCell(2));        // Role (Column 3)
            String gender = getCellValueAsString(row.getCell(3));      // Gender (Column 4)
            String age = getCellValueAsString(row.getCell(4));         // Age (Column 5)

            // Default password for all staff
            String password = "password";

            // Create user objects based on role
            switch (role.toLowerCase()) {
                case "doctor":
                    Doctor doctor = new Doctor(hospitalID, password, name, gender, age);
                    doctors.add(doctor);    // Add to the doctors list
                    users.add(doctor);      // Add to the users list for login purposes
                    break;

                case "pharmacist":
                    Pharmacist pharmacist = new Pharmacist(hospitalID, password, name, gender, age);
                    users.add(pharmacist);  // Add to the users list
                    break;

                case "administrator":
                    Administrator admin = new Administrator(hospitalID, password, name, gender, age);
                    users.add(admin);  // Add to the users list
                    break;

                default:
                    System.out.println("Unknown role in staff list: " + role);
                    break;
            }
        }

        workbook.close();
        file.close();


    }


        // Method to load patients from Excel file
    public void loadPatientsFromExcel() throws IOException {
        FileInputStream file = new FileInputStream(PATIENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip the header row

            String hospitalID = getCellValueAsString(row.getCell(0));  // Patient ID
            String name = getCellValueAsString(row.getCell(1));        // Name
            String dob = getCellValueAsString(row.getCell(2));         // Date of Birth
            String gender = getCellValueAsString(row.getCell(3));      // Gender
            String bloodType = getCellValueAsString(row.getCell(4));   // Blood Type
            String email = getCellValueAsString(row.getCell(5));       // Email
            String phoneNumber = getCellValueAsString(row.getCell(6)); // Phone Number

            // Create a new patient object and add it to the system
            Patient patient = new Patient(hospitalID, "password", name, dob, gender, email, phoneNumber, bloodType);
            patients.add(patient);
            users.add(patient);  // Make sure to add the patient to the 'users' list
        }

        workbook.close();
        file.close();
    }


        // Helper method to safely retrieve cell values as strings
        private String getCellValueAsString(Cell cell) {
            if (cell == null) {
                return "";  // Return an empty string if the cell is null
            }
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();  // Handle date cells
                    } else {
                        return String.valueOf((int) cell.getNumericCellValue());  // Convert numeric values to string
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                default:
                    return "";  // Return an empty string for other cell types
            }
        }
    
    

        // Method to load medications from Excel file
        private void loadMedicationsFromExcel() throws IOException {
            FileInputStream file = new FileInputStream(MEDICINE_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip the header row

                String medicationName = row.getCell(0).getStringCellValue();
                int stockLevel = (int) row.getCell(1).getNumericCellValue();
                int lowStockAlert = (int) row.getCell(2).getNumericCellValue();

                // Create a new medication object and add to the inventory
                Medication medication = new Medication(medicationName, stockLevel, lowStockAlert);
                inventory.add(medication);
            }

            workbook.close();
            file.close();
        }
        
     // Method for viewing patient records (doctor can only see their patients' records)
        public void viewPatientRecords(Doctor doctor) throws IOException {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Enter patient ID to view records:");
            String patientID = scanner.nextLine();

            // Open the Patient_List.xlsx file
            FileInputStream file = new FileInputStream(PATIENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming the data is on the first sheet

            boolean patientFound = false;

            // Iterate through rows in the sheet to find the patient by ID
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip the header row

                String excelPatientID = getCellValueAsString(row.getCell(0));  // Assuming patient ID is in column 1
                if (excelPatientID.equalsIgnoreCase(patientID)) {
                    patientFound = true;

                    // Retrieve patient details from the respective columns
                    String name = getCellValueAsString(row.getCell(1));  // Name in column 2
                    String dob = getCellValueAsString(row.getCell(2));   // Date of Birth in column 3
                    String gender = getCellValueAsString(row.getCell(3));  // Gender in column 4
                    String bloodType = getCellValueAsString(row.getCell(4));  // Blood Type in column 5
                    String contactInfo = getCellValueAsString(row.getCell(5));  // Contact in column 6
                    String pastDiagnoses = getCellValueAsString(row.getCell(6));  // Past diagnoses in column 7

                    // Display patient details
                    System.out.println("Patient Name: " + name);
                    System.out.println("Date of Birth: " + dob);
                    System.out.println("Gender: " + gender);
                    System.out.println("Blood Type: " + bloodType);
                    System.out.println("Contact: " + contactInfo);

                    // Display past diagnoses
                    if (pastDiagnoses == null || pastDiagnoses.isEmpty()) {
                        System.out.println("Past Diagnoses: No past diagnoses found.");
                    } else {
                        System.out.println("Past Diagnoses: " + pastDiagnoses);
                    }
                    break;
                }
            }

            if (!patientFound) {
                System.out.println("Patient not found.");
            }

            // Close the workbook and file
            workbook.close();
            file.close();
        }





     // Method for updating patient records
        public void updatePatientRecords(Doctor doctor) throws IOException {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Enter patient ID to update records:");
            String patientID = scanner.nextLine();

            // Open the Patient_List.xlsx file
            FileInputStream file = new FileInputStream(PATIENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming the data is on the first sheet

            boolean patientFound = false;

            // Iterate through rows in the sheet to find the patient by ID
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String excelPatientID = getCellValueAsString(row.getCell(0));  // Assuming patient ID is in column 1
                if (excelPatientID.equalsIgnoreCase(patientID)) {
                    patientFound = true;

                    // Retrieve the existing diagnoses from the "Past Diagnoses" column (assumed to be column 7)
                    String pastDiagnoses = getCellValueAsString(row.getCell(6));

                    System.out.println("Enter new diagnosis:");
                    String newDiagnosis = scanner.nextLine();

                    // Append the new diagnosis to the existing diagnoses
                    if (pastDiagnoses == null || pastDiagnoses.isEmpty()) {
                        pastDiagnoses = newDiagnosis;  // No previous diagnoses, just add the new one
                    } else {
                        pastDiagnoses = pastDiagnoses + ", " + newDiagnosis;  // Append the new diagnosis to the existing ones
                    }

                    // Update the "Past Diagnoses" column with the new diagnosis
                    Cell diagnosisCell = row.getCell(6);  // Assuming column 7 is the 'Past Diagnoses' column
                    if (diagnosisCell == null) {
                        diagnosisCell = row.createCell(6);
                    }
                    diagnosisCell.setCellValue(pastDiagnoses);

                    System.out.println("Diagnosis updated successfully.");
                    break;
                }
            }

            if (!patientFound) {
                System.out.println("Patient not found.");
            }

            // Write the updated data back to the Excel file
            file.close();
            FileOutputStream outputStream = new FileOutputStream(PATIENT_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        }


        // Method for viewing doctor's personal schedule (appointments)
        public void viewDoctorSchedule(Doctor doctor) throws IOException {
            System.out.println("Upcoming Appointments for Dr. " + doctor.getName() + ":");

            boolean hasAppointments = false;

            // Loop through all appointments to find those assigned to this doctor
            for (Appointment appointment : appointments) {
                if (appointment.getDoctor().equals(doctor) && appointment.getStatus().equalsIgnoreCase("confirmed")) {
                    hasAppointments = true;
                    System.out.println("Appointment with " + appointment.getPatient().getName() + " on " + appointment.getDateTime());
                }
            }

            if (!hasAppointments) {
                System.out.println("No upcoming appointments found.");
            }

            // Read and display the doctor's available slots from DocAvailability_List.xlsx
            FileInputStream file = new FileInputStream(DOC_AVAILABILITY_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            List<String> availability = new ArrayList<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip the header row

                Cell doctorIDCell = row.getCell(0);  // Doctor ID is in column 1
                Cell availabilityCell = row.getCell(1);  // Availability is in column 2

                // Check if the row corresponds to the current doctor
                if (doctorIDCell.getStringCellValue().equals(doctor.getHospitalID())) {
                    availability.add(availabilityCell.getStringCellValue());
                }
            }

            // Display the doctor's available slots
            System.out.println("\nAvailable Time Slots:");
            if (availability.isEmpty()) {
                System.out.println("No available slots.");
            } else {
                for (String slot : availability) {
                    System.out.println("- " + slot);
                }
            }

            // Close the workbook and file stream
            workbook.close();
            file.close();
        }


        // Method for setting doctor's availability for appointments
        public void setDoctorAvailability(Doctor doctor) throws IOException {
            Scanner scanner = new Scanner(System.in);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            dateFormat.setLenient(false);  // Disable lenient parsing to ensure strict format

            // Open the Excel file for doctor availability
            FileInputStream availabilityFile = new FileInputStream(DOC_AVAILABILITY_FILE_PATH);
            Workbook availabilityWorkbook = new XSSFWorkbook(availabilityFile);
            Sheet availabilitySheet = availabilityWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

            while (true) {
                System.out.println("Enter available time slot for appointments (format: yyyy-MM-dd HH:mm): ");
                String input = scanner.nextLine();

                // Validate the input date and time format
                try {
                    Date enteredDate = dateFormat.parse(input);  // Parse the input date

                    // Check if the entered date is in the past
                    Date currentDate = new Date();
                    if (enteredDate.before(currentDate)) {
                        System.out.println("The entered date and time is in the past. Please enter a future date.");
                    } else if (doctor.getAvailability().contains(input)) {
                        // Check if the slot is already in the doctor's availability list
                        System.out.println("This time slot has already been set. Please choose a different time.");
                    } else {
                        doctor.setAvailability(input);  // If valid, add it to the doctor's availability
                        System.out.println("Availability set for: " + input);

                        // Write the availability to the Excel file
                        int lastRowNum = availabilitySheet.getLastRowNum();
                        Row newRow = availabilitySheet.createRow(lastRowNum + 1);

                        newRow.createCell(0).setCellValue(doctor.getHospitalID());  // Doctor ID
                        newRow.createCell(1).setCellValue(input);  // Availability slot

                        // Save the changes to the Excel file
                        FileOutputStream outputStream = new FileOutputStream(DOC_AVAILABILITY_FILE_PATH);
                        availabilityWorkbook.write(outputStream);
                        outputStream.close();
                    }
                } catch (ParseException e) {
                    System.out.println("Invalid format. Please follow the format: yyyy-MM-dd HH:mm");
                }

                // Ask if they want to set another time slot
                System.out.println("Do you want to add another time slot? (yes/no): ");
                String addAnother = scanner.nextLine();
                if (!addAnother.equalsIgnoreCase("yes")) {
                    break;
                }
            }

            // Close the Excel workbook and file
            availabilityWorkbook.close();
            availabilityFile.close();
        }
    

        // Method for managing appointment requests (accept or decline)
        public void manageAppointmentRequests(Doctor doctor) throws IOException {
            Scanner scanner = new Scanner(System.in);

            // Open the Excel file to read the appointment list
            FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
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
            FileOutputStream outputStream = new FileOutputStream(APPOINTMENT_FILE_PATH);
            workbook.write(outputStream);

            // Close all resources
            workbook.close();
            file.close();
            outputStream.close();
        }
        
        public void viewAvailableAppointmentSlots() throws IOException {
            FileInputStream file = new FileInputStream(DOC_AVAILABILITY_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains doctor availability data

            System.out.println("Available Appointment Slots:");

            // Iterate through the rows to list available slots
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip the header row

                // Read the doctor ID and available date/time slot
                String doctorID = getCellValueAsString(row.getCell(0));  // Assuming doctor ID is in column 1
                String appointmentDate = getCellValueAsString(row.getCell(1));  // Assuming appointment slot is in column 2

                // Display available slots
                System.out.println("Doctor ID: " + doctorID + " | Available Slot: " + appointmentDate);
            }

            workbook.close();
            file.close();
        }

        // 2. Schedule an appointment for a patient
        public void scheduleAppointment(Patient patient) throws IOException {
            Scanner scanner = new Scanner(System.in);

            // Open the Excel file to read doctor availability
            FileInputStream file = new FileInputStream(DOC_AVAILABILITY_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            // Store the available slots for all doctors
            List<String> allAvailableSlots = new ArrayList<>();
            List<Doctor> allDoctors = new ArrayList<>();
            List<String> correspondingDoctorIDs = new ArrayList<>();

            // Read through the sheet and list all available slots for all doctors
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String doctorID = row.getCell(0).getStringCellValue();  // Doctor ID in column 1
                String availabilitySlot = row.getCell(1).getStringCellValue();  // Availability slot in column 2

                // Find the corresponding doctor object from the list of doctors
                Doctor doctor = findDoctorByID(doctorID);

                if (doctor != null) {
                    allAvailableSlots.add(availabilitySlot);
                    allDoctors.add(doctor);
                    correspondingDoctorIDs.add(doctorID);
                }
            }

            // If no available slots, inform the patient
            if (allAvailableSlots.isEmpty()) {
                System.out.println("No available slots for any doctor.");
                workbook.close();
                file.close();
                return;  // Return to the main menu
            }

            // Display all available slots for all doctors
            System.out.println("Available slots for all doctors:");
            for (int i = 0; i < allAvailableSlots.size(); i++) {
                System.out.println((i + 1) + ". Dr. " + allDoctors.get(i).getName() + " - " + allAvailableSlots.get(i));
            }

            System.out.print("Choose a slot by entering the number: ");
            if (scanner.hasNextInt()) {
                int slotChoice = scanner.nextInt();
                scanner.nextLine();  // Consume the newline

                if (slotChoice < 1 || slotChoice > allAvailableSlots.size()) {
                    System.out.println("Invalid choice. Returning to the main menu.");
                    workbook.close();
                    file.close();
                    return;  // Return to the main menu
                }

                // Get the selected slot and doctor
                String chosenSlot = allAvailableSlots.get(slotChoice - 1);
                Doctor selectedDoctor = allDoctors.get(slotChoice - 1);

                // Generate a new appointment and mark the slot as unavailable
                Appointment newAppointment = new Appointment(generateAppointmentID(), patient, selectedDoctor, chosenSlot, "pending", new ArrayList<>());
                appointments.add(newAppointment);  // Add appointment to the list
                removeDoctorAvailabilityFromExcel(selectedDoctor.getHospitalID(), chosenSlot);  // Mark the slot as unavailable in Excel

                System.out.println("Appointment scheduled successfully with Dr. " + selectedDoctor.getName() + " at " + chosenSlot);

                // Record the new appointment in Appointment_List.xlsx
                recordAppointmentInExcel(newAppointment);  // Call the method to write to the Excel file
            } else {
                // Invalid input: return to the main menu
                System.out.println("Invalid input. Returning to the main menu.");
                scanner.next();  // Consume the invalid input
            }

            workbook.close();
            file.close();
        }

        // Helper method to find a doctor by ID from the list of doctors
        private Doctor findDoctorByID(String doctorID) {
            for (Doctor doctor : doctors) {
                if (doctor.getHospitalID().equalsIgnoreCase(doctorID)) {
                    return doctor;
                }
            }
            return null;
        }

        // Method to record the new appointment in Appointment_List.xlsx
        public void recordAppointmentInExcel(Appointment appointment) throws IOException {

            FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            // Find the next available row in the sheet
            int lastRowNum = sheet.getLastRowNum();
            Row newRow = sheet.createRow(lastRowNum + 1);

            // Write appointment details to the new row
            newRow.createCell(0).setCellValue(appointment.getDoctor().getHospitalID()); 
            newRow.createCell(1).setCellValue(appointment.getPatient().getHospitalID()); 
            newRow.createCell(2).setCellValue(appointment.getAppointmentID());  // Appointment ID
            newRow.createCell(3).setCellValue(appointment.getDoctor().getName());  // Doctor name
            newRow.createCell(4).setCellValue(appointment.getPatient().getName());  // Patient name
            newRow.createCell(5).setCellValue(appointment.getDateTime());  // Date of appointment
            newRow.createCell(6).setCellValue(appointment.getStatus());  // Appointment status

            // Write the updated workbook back to the Excel file
            FileOutputStream outputStream = new FileOutputStream(APPOINTMENT_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            file.close();

        }

        

        // Helper method to remove a doctor's availability from Excel after booking
     // Helper method to remove a doctor's availability from Excel after booking
        private void removeDoctorAvailabilityFromExcel(String doctorID, String slot) throws IOException {
            FileInputStream file = new FileInputStream(DOC_AVAILABILITY_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            boolean found = false;  // Flag to track if we found the row to delete
            int lastRowNum = sheet.getLastRowNum();  // Get the last row number

            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;  // Skip empty rows

                String currentDoctorID = row.getCell(0).getStringCellValue();
                String currentSlot = row.getCell(1).getStringCellValue();

                // Check if this is the doctor and slot to remove
                if (currentDoctorID.equalsIgnoreCase(doctorID) && currentSlot.equals(slot)) {
                    found = true;

                    // Shift all rows up to remove the empty row
                    if (i < lastRowNum) {
                        sheet.shiftRows(i + 1, lastRowNum, -1);  // Shift rows up from the next row to the last row
                    } else {
                        sheet.removeRow(row);  // Remove the last row if it's the only one
                    }

                    break;
                }
            }

            if (found) {
                // Write the changes back to the Excel file
                FileOutputStream outputStream = new FileOutputStream(DOC_AVAILABILITY_FILE_PATH);
                workbook.write(outputStream);
                outputStream.close();
            }

            workbook.close();
            file.close();
        }

    


        // 3. Reschedule an appointment for a patient
        public void rescheduleAppointment(Patient patient) throws IOException {
            Scanner scanner = new Scanner(System.in);

            // Open Appointment_List.xlsx to fetch current appointments
            FileInputStream appointmentFile = new FileInputStream(APPOINTMENT_FILE_PATH);
            Workbook appointmentWorkbook = new XSSFWorkbook(appointmentFile);
            Sheet appointmentSheet = appointmentWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

            List<Row> patientAppointments = new ArrayList<>();

            // Display the patient's current appointments
            System.out.println("Your scheduled appointments:");
            boolean hasAppointments = false;
            for (Row row : appointmentSheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String patientID = getCellValueAsString(row.getCell(1));  // Assuming patient ID is in column 2
                String status = getCellValueAsString(row.getCell(6));     // Assuming status is in column 7

                if (patient.getHospitalID().equalsIgnoreCase(patientID)) {
                    hasAppointments = true;
                    patientAppointments.add(row);
                    String appointmentID = getCellValueAsString(row.getCell(2));  // Assuming appointment ID is in column 3
                    String dateTime = getCellValueAsString(row.getCell(5));       // Assuming date/time is in column 6
                    System.out.println((patientAppointments.size()) + ". Appointment ID: " + appointmentID + " | Date/Time: " + dateTime);
                }
            }

            if (!hasAppointments) {
                System.out.println("You have no appointments to reschedule.");
                appointmentWorkbook.close();
                appointmentFile.close();
                return;
            }

            // Ask the patient to select an appointment to reschedule
            int appointmentChoice = -1;
            while (appointmentChoice < 1 || appointmentChoice > patientAppointments.size()) {
                System.out.print("Choose an appointment to reschedule by entering the number: ");
                if (scanner.hasNextInt()) {
                    appointmentChoice = scanner.nextInt();
                    if (appointmentChoice < 1 || appointmentChoice > patientAppointments.size()) {
                        System.out.println("Invalid choice. Please choose a valid appointment number.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next();  // Clear invalid input
                }
            }
            scanner.nextLine();  // Consume the newline

            // Get the selected appointment
            Row selectedAppointmentRow = patientAppointments.get(appointmentChoice - 1);
            String oldDateTime = getCellValueAsString(selectedAppointmentRow.getCell(5));  // Assuming old slot in column 6
            String doctorID = getCellValueAsString(selectedAppointmentRow.getCell(0));     // Assuming doctor ID in column 1

            // Show available slots for the selected doctor from DocAvailability_List.xlsx
            FileInputStream availabilityFile = new FileInputStream(DOC_AVAILABILITY_FILE_PATH);
            Workbook availabilityWorkbook = new XSSFWorkbook(availabilityFile);
            Sheet availabilitySheet = availabilityWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

            List<String> availableSlots = new ArrayList<>();
            List<Row> availableRows = new ArrayList<>();

            System.out.println("Available slots for Doctor ID: " + doctorID);
            for (Row row : availabilitySheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String availableDoctorID = getCellValueAsString(row.getCell(0));  // Assuming Doctor ID in column 1
                if (availableDoctorID.equalsIgnoreCase(doctorID)) {
                    String availabilitySlot = getCellValueAsString(row.getCell(1));  // Assuming availability slot in column 2
                    availableSlots.add(availabilitySlot);
                    availableRows.add(row);  // Keep track of the row for later update
                    System.out.println((availableSlots.size()) + ". " + availabilitySlot);
                }
            }

            if (availableSlots.isEmpty()) {
                System.out.println("No available slots for this doctor.");
                availabilityWorkbook.close();
                availabilityFile.close();
                appointmentWorkbook.close();
                appointmentFile.close();
                return;
            }

            // Ask the patient to select a new time slot
            int slotChoice = -1;
            while (slotChoice < 1 || slotChoice > availableSlots.size()) {
                System.out.print("Choose a new slot by entering the number: ");
                if (scanner.hasNextInt()) {
                    slotChoice = scanner.nextInt();
                    if (slotChoice < 1 || slotChoice > availableSlots.size()) {
                        System.out.println("Invalid choice. Please choose a valid slot number.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next();  // Clear invalid input
                }
            }
            scanner.nextLine();  // Consume the newline

            String newDateTime = availableSlots.get(slotChoice - 1);
            Row newSlotRow = availableRows.get(slotChoice - 1);

            // Update the Appointment_List.xlsx with the new time slot
            selectedAppointmentRow.getCell(5).setCellValue(newDateTime);  // Update appointment time (assuming it's in column 6)

            // Add the old slot back to DocAvailability_List.xlsx
            Row newRow = availabilitySheet.createRow(availabilitySheet.getLastRowNum() + 1);
            newRow.createCell(0).setCellValue(doctorID);  // Doctor ID
            newRow.createCell(1).setCellValue(oldDateTime);  // Old time slot becomes available

            // Remove the newly selected slot from DocAvailability_List.xlsx without leaving an empty row
            int rowIndex = newSlotRow.getRowNum();
            if (rowIndex >= 0 && rowIndex < availabilitySheet.getLastRowNum()) {
                availabilitySheet.shiftRows(rowIndex + 1, availabilitySheet.getLastRowNum(), -1);
            } else {
                availabilitySheet.removeRow(newSlotRow);  // Remove the last row without shifting
            }

            // Write changes back to the Excel files
            availabilityFile.close();  // Close input stream before writing
            FileOutputStream availabilityOutputStream = new FileOutputStream(DOC_AVAILABILITY_FILE_PATH);
            availabilityWorkbook.write(availabilityOutputStream);
            availabilityWorkbook.close();
            availabilityOutputStream.close();

            FileOutputStream appointmentOutputStream = new FileOutputStream(APPOINTMENT_FILE_PATH);
            appointmentWorkbook.write(appointmentOutputStream);
            appointmentWorkbook.close();
            appointmentOutputStream.close();

            System.out.println("Appointment rescheduled successfully to " + newDateTime);
        }





        // 4. Cancel an appointment for a patient
        public void cancelAppointment(Patient patient) throws IOException {
            Scanner scanner = new Scanner(System.in);

            // Open the Appointment_List.xlsx file
            FileInputStream appointmentFile = new FileInputStream(APPOINTMENT_FILE_PATH);
            Workbook appointmentWorkbook = new XSSFWorkbook(appointmentFile);
            Sheet appointmentSheet = appointmentWorkbook.getSheetAt(0);  // Assuming the first sheet

            List<Row> cancelableAppointments = new ArrayList<>();
            
            // Display only "pending" or "confirmed" appointments for the patient
            System.out.println("Your pending or confirmed appointments:");
            boolean hasCancelableAppointments = false;

            for (Row row : appointmentSheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String patientID = getCellValueAsString(row.getCell(1));  // Assuming Patient ID is in column 2
                String status = getCellValueAsString(row.getCell(6));  // Assuming Status is in column 7
                String appointmentID = getCellValueAsString(row.getCell(2));  // Assuming Appointment ID is in column 3
                String dateTime = getCellValueAsString(row.getCell(5));  // Assuming Date/Time is in column 6

                // Check if the appointment belongs to the logged-in patient and is either "pending" or "confirmed"
                if (patient.getHospitalID().equalsIgnoreCase(patientID) && 
                    (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("confirmed"))) {
                    
                    hasCancelableAppointments = true;
                    cancelableAppointments.add(row);  // Add to the list of cancelable appointments
                    System.out.println((cancelableAppointments.size()) + ". Appointment ID: " + appointmentID + " | Date/Time: " + dateTime + " | Status: " + status);
                }
            }

            if (!hasCancelableAppointments) {
                System.out.println("You have no pending or confirmed appointments to cancel.");
                appointmentWorkbook.close();
                appointmentFile.close();
                return;
            }

            // Ask the patient to select an appointment to cancel
            int appointmentChoice = -1;
            while (appointmentChoice < 1 || appointmentChoice > cancelableAppointments.size()) {
                System.out.print("Choose an appointment to cancel by entering the number: ");
                if (scanner.hasNextInt()) {
                    appointmentChoice = scanner.nextInt();
                    if (appointmentChoice < 1 || appointmentChoice > cancelableAppointments.size()) {
                        System.out.println("Invalid choice. Please choose a valid appointment number.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next();  // Clear invalid input
                }
            }
            scanner.nextLine();  // Consume the newline

            // Get the selected appointment row
            Row selectedAppointmentRow = cancelableAppointments.get(appointmentChoice - 1);
            String doctorID = getCellValueAsString(selectedAppointmentRow.getCell(0));  // Assuming Doctor ID is in column 1
            String appointmentSlot = getCellValueAsString(selectedAppointmentRow.getCell(5));  // Assuming Date/Time is in column 6

            // Cancel the appointment by setting the status to "cancelled"
            selectedAppointmentRow.getCell(6).setCellValue("cancelled");  // Update status to "cancelled" in column 7

            // Write the updated status back to the Appointment_List.xlsx file
            appointmentFile.close();  // Close input stream before writing
            FileOutputStream appointmentOutStream = new FileOutputStream(APPOINTMENT_FILE_PATH);
            appointmentWorkbook.write(appointmentOutStream);
            appointmentWorkbook.close();
            appointmentOutStream.close();

            // Add the appointment slot back to the doctor's availability in DocAvailability_List.xlsx
            if (doctorID != null && appointmentSlot != null) {
                addSlotBackToDoctorAvailability(doctorID, appointmentSlot);
            }

            System.out.println("Appointment cancelled successfully.");
        }


        // Method to add slot back to DocAvailability_List.xlsx
        private void addSlotBackToDoctorAvailability(String doctorID, String appointmentSlot) throws IOException {
            FileInputStream availabilityFile = new FileInputStream(DOC_AVAILABILITY_FILE_PATH);
            Workbook availabilityWorkbook = new XSSFWorkbook(availabilityFile);
            Sheet availabilitySheet = availabilityWorkbook.getSheetAt(0);  // Assuming first sheet

            // Add the appointment slot back to the availability list for the doctor
            int lastRowNum = availabilitySheet.getLastRowNum();
            Row newRow = availabilitySheet.createRow(lastRowNum + 1);

            newRow.createCell(0).setCellValue(doctorID);  // Doctor ID in column 1
            newRow.createCell(1).setCellValue(appointmentSlot);  // Appointment slot in column 2

            // Write changes to DocAvailability_List.xlsx
            FileOutputStream availabilityOutStream = new FileOutputStream(DOC_AVAILABILITY_FILE_PATH);
            availabilityWorkbook.write(availabilityOutStream);
            availabilityWorkbook.close();
            availabilityOutStream.close();
            availabilityFile.close();
        }


        // 5. View scheduled appointments for a patient
        public void viewScheduledAppointments(Patient patient) throws IOException {
            System.out.println("Your scheduled appointments:");

            // Open the Excel file to read the appointment list
            FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            boolean hasAppointments = false;  // Flag to check if appointments are found

            // Loop through the rows in the sheet to find appointments for this patient
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                // Check if the patient ID in the Excel file matches the logged-in patient's ID
                String patientIDInExcel = row.getCell(1).getStringCellValue();  // Assuming Patient ID is in column 3
                if (patientIDInExcel.equalsIgnoreCase(patient.getHospitalID())) {
                    hasAppointments = true;

                    // Get Appointment details from the respective columns
                    String appointmentID = row.getCell(2).getStringCellValue();  // Appointment ID (Column 3)
                    String doctorName = row.getCell(3).getStringCellValue();     // Doctor Name (Column 4)
                    String dateTime = row.getCell(5).getStringCellValue();       // Date/Time (Column 6)
                    String status = row.getCell(6).getStringCellValue();         // Status (Column 7)

                    // Display the appointment details
                    System.out.println("Appointment ID: " + appointmentID + " | Doctor: Dr. " + doctorName + " | Date/Time: " + dateTime + " | Status: " + status);
                }
            }

            // Close workbook and file
            workbook.close();
            file.close();

            if (!hasAppointments) {
                System.out.println("You have no scheduled appointments.");
            }
        }

        // Helper method to generate unique appointment IDs
        private String generateAppointmentID() {
            String newAppointmentID = "APT" + nextAppointmentNumber;
            nextAppointmentNumber++;  // Increment for the next appointment
            return newAppointmentID;
        }


        // Helper method to get appointments for a specific patient
        private List<Appointment> getAppointmentsForPatient(Patient patient) {
            List<Appointment> patientAppointments = new ArrayList<>();
            for (Appointment appointment : appointments) {
                if (appointment.getPatient().equals(patient)) {
                    patientAppointments.add(appointment);
                }
            }
            return patientAppointments;
        }
        
        public void viewAppointmentOutcomeRecord() throws IOException {
            // Open the Appointment_List.xlsx file
            FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains appointment data

            System.out.println("Viewing appointment outcome records...");

            // Loop through the rows in the sheet
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip the header row

                // Retrieve necessary information from each row
                String appointmentID = getCellValueAsString(row.getCell(2));  // Appointment ID (Column 3)
                String patientName = getCellValueAsString(row.getCell(4));    // Patient Name (Column 1)
                String doctorName = getCellValueAsString(row.getCell(3));     // Doctor Name (Column 4)
                String date = getCellValueAsString(row.getCell(5));           // Appointment Date (Column 6)
                String medications = getCellValueAsString(row.getCell(9));    // Prescribed Medications (Column 5)
                String status = getCellValueAsString(row.getCell(6)); 
                
                // Display the information
                if (status.equalsIgnoreCase("completed") || status.equalsIgnoreCase("paid")) {
                    System.out.println("Appointment ID: " + appointmentID);
                    System.out.println("Patient Name: " + patientName);
                    System.out.println("Doctor Name: " + doctorName);
                    System.out.println("Date: " + date);
                    System.out.println("Prescribed Medications: " + medications);
                    System.out.println("------------------------------------");                	
                }

            }

            workbook.close();
            file.close();
        }


        // 2. Update Prescription Status (e.g., from "pending" to "dispensed")
        public void updatePrescriptionStatus() throws IOException {
            Scanner scanner = new Scanner(System.in);

            // Open the Excel file
            FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            List<Row> pendingAppointments = new ArrayList<>();

            // List all appointments with prescription status "pending"
            System.out.println("Pending Prescriptions to be dispensed:");
            boolean hasPendingPrescriptions = false;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String prescriptionStatus = getCellValueAsString(row.getCell(10));  // Assuming Prescription Status is in column 10

                if (prescriptionStatus.equalsIgnoreCase("pending")) {
                    hasPendingPrescriptions = true;
                    pendingAppointments.add(row);  // Add the row to the list of pending appointments
                    
                    String appointmentID = getCellValueAsString(row.getCell(2));  // Assuming Appointment ID is in column 3
                    String patientName = getCellValueAsString(row.getCell(1));    // Assuming Patient Name is in column 5
                    String dateTime = getCellValueAsString(row.getCell(5));       // Assuming Date/Time is in column 6
                    String meds = getCellValueAsString(row.getCell(9));  
                    System.out.println((pendingAppointments.size()) + ". Appointment ID: " + appointmentID + " | Patient: " + patientName + " | Date/Time: " + dateTime + " | Prescribed Medications (Pending): " + meds);
                }
            }

            if (!hasPendingPrescriptions) {
                System.out.println("No pending prescriptions found.");
                workbook.close();
                file.close();
                return;
            }

            // Validate user input for choosing an appointment to update
            int choice = -1;
            while (true) {
                System.out.print("Choose an appointment to update by entering the number: ");
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();  // Consume the newline

                    if (choice >= 1 && choice <= pendingAppointments.size()) {
                        break;  // Valid input, exit loop
                    } else {
                        System.out.println("Invalid choice. Please enter a valid number between 1 and " + pendingAppointments.size());
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.next();  // Clear the invalid input
                }
            }

            // Get the selected appointment row
            Row selectedRow = pendingAppointments.get(choice - 1);

            // Update the prescription status to "dispensed"
            createOrUpdateCell(selectedRow, 10, "dispensed");  // Update Prescription Status to "dispensed"
            System.out.println("Prescription status updated to 'dispensed'.");

            // Write changes back to the Excel file
            file.close();
            FileOutputStream outputStream = new FileOutputStream(APPOINTMENT_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        }


        // 3. View Medication Inventory for pharmacists
        public void viewMedicationInventory() throws IOException {
            FileInputStream file = new FileInputStream(MEDICINE_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            System.out.println("Medication Inventory:");
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String medicationName = row.getCell(0).getStringCellValue();
                int stockLevel = (int) row.getCell(1).getNumericCellValue();
                int lowStockAlert = (int) row.getCell(2).getNumericCellValue();

                System.out.println("Medication: " + medicationName + " | Stock Level: " + stockLevel + " | Low Stock Alert: " + lowStockAlert);
            }

            workbook.close();
            file.close();
        }


        // 4. Submit a Replenishment Request for medications with low stock
     // Search for a medication by name in the inventory
        public Medication getMedicationByName(String medicationName) {
            for (Medication medication : inventory) {
                if (medication.getName().equalsIgnoreCase(medicationName)) {
                    return medication;  // Medication found
                }
            }
            return null;  // Medication not found
        }

        public void submitReplenishmentRequest(Pharmacist pharmacist) throws IOException {
            Scanner scanner = new Scanner(System.in);

            // Reload medication inventory from Excel to ensure we have the latest stock levels
            FileInputStream file = new FileInputStream(MEDICINE_FILE_PATH);
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
            FileInputStream file = new FileInputStream(REPLENISHMENT_FILE_PATH);
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
            FileOutputStream outputStream = new FileOutputStream(REPLENISHMENT_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            file.close();
        }






        
        public void viewUpcomingAppointments(Doctor doctor) throws IOException {
            // Open the Appointment_List.xlsx file
            FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date currentDate = new Date();
            boolean hasUpcomingAppointments = false;

            System.out.println("Upcoming Appointments for Dr. " + doctor.getName() + ":");

            // Iterate over the rows in the Excel file
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                // Extract relevant data from the row
                String doctorID = row.getCell(0).getStringCellValue();  // Assuming doctor ID is in column 2
                String status = row.getCell(6).getStringCellValue();    // Assuming status is in column 7
                String appointmentDateStr = row.getCell(5).getStringCellValue();  // Assuming date is in column 6

                try {
                    Date appointmentDate = dateFormat.parse(appointmentDateStr);  // Parse the date from the string

                    // Check if the appointment is for this doctor, has a "confirmed" status, and is in the future
                    if (doctorID.equals(doctor.getHospitalID()) && status.equalsIgnoreCase("confirmed") && appointmentDate.after(currentDate)) {
                        hasUpcomingAppointments = true;

                        // Extract and display relevant appointment details
                        String appointmentID = row.getCell(2).getStringCellValue();  // Assuming appointment ID is in column 3
                        String patientID = row.getCell(1).getStringCellValue();      // Assuming patient ID is in column 4
                        String patientName = row.getCell(4).getStringCellValue();    // Assuming patient name is in column 5

                        System.out.println("Appointment ID: " + appointmentID);
                        System.out.println("Patient ID: " + patientID);
                        System.out.println("Patient Name: " + patientName);
                        System.out.println("Appointment Date: " + appointmentDateStr);
                        System.out.println("-----------------------------------------");
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing date for appointment: " + row.getCell(2).getStringCellValue());
                }
            }

            if (!hasUpcomingAppointments) {
                System.out.println("No upcoming appointments found.");
            }

            workbook.close();
            file.close();
        }
        
        public void recordAppointmentOutcome(Doctor doctor) throws IOException {
            Scanner scanner = new Scanner(System.in);

            // Open the Excel file
            FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains appointment data

            // Display all confirmed appointments for the doctor
            System.out.println("Confirmed Appointments for Dr. " + doctor.getName() + ":");
            boolean hasConfirmedAppointments = false;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String appointmentID = getCellValueAsString(row.getCell(2));  // Assuming Appointment ID is in column 3
                String doctorID = getCellValueAsString(row.getCell(0));  // Assuming Doctor ID is in column 2
                String status = getCellValueAsString(row.getCell(6));  // Assuming Status is in column 7

                if (doctor.getHospitalID().equals(doctorID) && status.equalsIgnoreCase("confirmed")) {
                    hasConfirmedAppointments = true;
                    System.out.println("Appointment ID: " + appointmentID + " | Date/Time: " + getCellValueAsString(row.getCell(5)));

                    System.out.println("Do you want to record the outcome for this appointment? (yes/no)");
                    String decision = scanner.nextLine();

                    if (decision.equalsIgnoreCase("yes")) {
                        System.out.println("Enter the type of service provided (e.g., consultation, X-ray):");
                        String serviceProvided = scanner.nextLine();

                        System.out.println("Enter any prescribed medications (comma-separated if multiple):");
                        String prescribedMedicationsInput = scanner.nextLine();
                        
                        System.out.println("Enter consultation notes:");
                        String consultationNotes = scanner.nextLine();
                        
                        // Create cells if they don't exist
                        createOrUpdateCell(row, 7, serviceProvided);  // Column 4: Service Provided
                        createOrUpdateCell(row, 9, prescribedMedicationsInput);  // Column 5: Prescribed Medications
                        createOrUpdateCell(row, 8, consultationNotes);  // Column 9: Consultation Notes
                        createOrUpdateCell(row, 6, "completed");  // Column 7: Set appointment status to 'completed'
                        if (!prescribedMedicationsInput.equalsIgnoreCase("nil")) {
                            createOrUpdateCell(row, 10, "pending");  // Column 10: Prescription Status
                        }
                        System.out.println("Appointment outcome recorded successfully.");
                    }
                }
            }

            if (!hasConfirmedAppointments) {
                System.out.println("No confirmed appointments available.");
            }

            // Write changes back to the Excel file
            file.close();
            FileOutputStream outputStream = new FileOutputStream(APPOINTMENT_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        }

        // Helper method to create or update a cell
        private void createOrUpdateCell(Row row, int columnIndex, String value) {
            Cell cell = row.getCell(columnIndex);
            if (cell == null) {
                cell = row.createCell(columnIndex);
            }
            cell.setCellValue(value);
        }






        
        
        public void viewPastAppointmentOutcomes(Patient patient) throws IOException {
            // Open the Appointment_List.xlsx file
            FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains appointment data

            System.out.println("Past Appointment Outcomes for " + patient.getName() + ":");

            boolean hasPastAppointments = false;  // Flag to track if there are any past completed appointments

            // Loop through the rows in the Excel sheet
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip the header row

                // Read the patient ID from the second column (assuming patient ID is in column 1)
                String patientID = getCellValueAsString(row.getCell(1));

                // Check if the appointment belongs to the logged-in patient and if the status is "completed"
                String status = getCellValueAsString(row.getCell(6));  // Assuming status is in column 7
                if (patient.getHospitalID().equalsIgnoreCase(patientID) && (status.equalsIgnoreCase("completed") || status.equalsIgnoreCase("paid"))) {
                    hasPastAppointments = true;

                    // Retrieve and display the appointment details
                    String doctorName = getCellValueAsString(row.getCell(3));  // Assuming doctor name is in column 4
                    String date = getCellValueAsString(row.getCell(5));         // Assuming date is in column 6
                    String serviceProvided = getCellValueAsString(row.getCell(7));  // Assuming service provided is in column 8
                    String prescribedMedications = getCellValueAsString(row.getCell(9));  // Assuming medications are in column 10
                    String consultationNotes = getCellValueAsString(row.getCell(8));  // Assuming consultation notes are in column 9
                   

                    // Display the past appointment details
                    System.out.println("Doctor: " + doctorName);
                    System.out.println("Date: " + date);
                    System.out.println("Service Provided: " + serviceProvided);
                    System.out.println("Prescribed Medications: " + prescribedMedications);
                    System.out.println("Consultation Notes: " + consultationNotes);
                    if(status.equalsIgnoreCase("completed")) {
                    	System.out.println("Status: Completed (Unpaid)");
                    }else {
                    	System.out.println("Status: " + status);
                    }
                    
                    System.out.println("------------------------------------------");
                }
            }

            // If no past appointments are found, show a message
            if (!hasPastAppointments) {
                System.out.println("No past appointment outcomes found.");
            }

            // Close the workbook and file
            workbook.close();
            file.close();
        }

        
        // Method to update patient information in the Excel file
        public void updatePatientInfoInExcel(Patient patient) throws IOException {
            FileInputStream file = new FileInputStream(PATIENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            // Iterate over the rows to find the patient
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                Cell idCell = row.getCell(0);  // Assuming patient ID is in the first column
                if (idCell.getStringCellValue().equalsIgnoreCase(patient.getHospitalID())) {
                    // Found the patient, update email and phone number
                    row.getCell(5).setCellValue(patient.getEmail());  // Assuming email is in column 6
                    row.getCell(6).setCellValue(patient.getPhoneNumber());  // Assuming phone number is in column 7
                    break;
                }
            }

            // Write the updated workbook back to the Excel file
            FileOutputStream outputStream = new FileOutputStream(PATIENT_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            file.close();
        }

        
        public void viewHospitalStaff() throws IOException {
            FileInputStream file = new FileInputStream(STAFF_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            System.out.println("Hospital Staff List:");
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String staffID = row.getCell(0).getStringCellValue();
                String name = row.getCell(1).getStringCellValue();
                String role = row.getCell(2).getStringCellValue();
                String gender = row.getCell(3).getStringCellValue();
                int age = (int) row.getCell(4).getNumericCellValue();

                System.out.println("ID: " + staffID + ", Name: " + name + ", Role: " + role + ", Gender: " + gender + ", Age: " + age);
            }

            workbook.close();
            file.close();
        }
        
        public void addStaffMember(Scanner scanner) throws IOException {
            // Open the Excel file for staff
            FileInputStream staffFile = new FileInputStream(STAFF_FILE_PATH);
            Workbook staffWorkbook = new XSSFWorkbook(staffFile);
            Sheet staffSheet = staffWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

            String staffID;
            boolean uniqueID;

            // Loop until a unique staff ID is provided
            do {
                System.out.println("Enter staff ID:");
                staffID = scanner.nextLine().trim();

                uniqueID = true;  // Assume it's unique unless we find a conflict

                // Check if the staff ID already exists in the Staff_List.xlsx
                for (Row row : staffSheet) {
                    if (row.getRowNum() == 0) continue;  // Skip header row

                    String existingStaffID = row.getCell(0).getStringCellValue();  // Assuming staff ID is in column 0
                    if (existingStaffID.equalsIgnoreCase(staffID)) {
                        uniqueID = false;
                        System.out.println("Error: Staff ID already exists. Please enter a unique Staff ID.");
                        break;
                    }
                }
            } while (!uniqueID);  // Repeat until a unique staff ID is provided

            // Role validation loop
            String role;
            while (true) {
                System.out.println("Enter staff role (doctor/pharmacist/administrator):");
                role = scanner.nextLine().trim().toLowerCase();
                if (role.equals("doctor") || role.equals("pharmacist") || role.equals("administrator")) {
                    break;  // Valid role input
                } else {
                    System.out.println("Invalid role. Please enter 'doctor', 'pharmacist', or 'administrator'.");
                }
            }

            System.out.println("Enter staff name:");
            String name = scanner.nextLine();

            // Gender validation loop
            String gender;
            while (true) {
                System.out.println("Enter gender (Male/Female/Others):");
                gender = scanner.nextLine().trim();
                if (gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase("Others")) {
                    break;  // Valid gender input
                } else {
                    System.out.println("Invalid input. Please enter 'Male', 'Female', or 'Others'.");
                }
            }

            // Age validation loop
            int age = -1;
            boolean validInput = false;
            while (!validInput) {
                System.out.println("Enter age:");
                if (scanner.hasNextInt()) {
                    age = scanner.nextInt();
                    if (age >= 0) {
                        validInput = true;  // Valid age input
                    } else {
                        System.out.println("Invalid input. Age must be a non-negative integer.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid age.");
                    scanner.next();  // Clear the invalid input
                }
            }
            scanner.nextLine();  // Consume the newline

            // Add to internal system lists
            switch (role.toLowerCase()) {
                case "doctor":
                    Doctor doctor = new Doctor(staffID, "password", name, gender, Integer.toString(age));
                    users.add(doctor);
                    doctors.add(doctor);
                    break;
                case "pharmacist":
                    Pharmacist pharmacist = new Pharmacist(staffID, "password", name, gender, Integer.toString(age));
                    users.add(pharmacist);
                    pharmacists.add(pharmacist);
                    break;
                case "administrator":
                    Administrator admin = new Administrator(staffID, "password", name, gender, Integer.toString(age));
                    users.add(admin);
                    break;
                default:
                    System.out.println("Invalid role.");
                    staffWorkbook.close();
                    staffFile.close();
                    return;
            }

            // Add new staff to Staff_List.xlsx
            int lastRowNum = staffSheet.getLastRowNum();
            Row newRow = staffSheet.createRow(lastRowNum + 1);

            newRow.createCell(0).setCellValue(staffID);
            newRow.createCell(1).setCellValue(name);
            newRow.createCell(2).setCellValue(role);
            newRow.createCell(3).setCellValue(gender);
            newRow.createCell(4).setCellValue(age);

            // Write back to the Staff_List.xlsx file
            FileOutputStream staffOutputStream = new FileOutputStream(STAFF_FILE_PATH);
            staffWorkbook.write(staffOutputStream);
            staffWorkbook.close();
            staffOutputStream.close();
            staffFile.close();

            // Now append the new staff's authentication details to Authentication_List.xlsx
            appendAuthenticationToExcel(staffID);

            System.out.println("Staff member added successfully.");
        }



        
        private void appendAuthenticationToExcel(String staffID) throws IOException {
            // Open the Authentication_List.xlsx file
            FileInputStream authFile = new FileInputStream(AUTHENTICATION_FILE_PATH);
            Workbook authWorkbook = new XSSFWorkbook(authFile);
            Sheet authSheet = authWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

            // Create a new row at the end of the sheet
            int lastRowNum = authSheet.getLastRowNum();
            Row newRow = authSheet.createRow(lastRowNum + 1);

            // Add staff ID and default password
            newRow.createCell(0).setCellValue(staffID);      // Staff ID
            newRow.createCell(1).setCellValue("password");   // Default password

            // Write back to the Authentication_List.xlsx file
            FileOutputStream authOutputStream = new FileOutputStream(AUTHENTICATION_FILE_PATH);
            authWorkbook.write(authOutputStream);
            authWorkbook.close();
            authOutputStream.close();
            authFile.close();
        }







        public void updateStaffMember(Scanner scanner) throws IOException {
            FileInputStream file = new FileInputStream(STAFF_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            System.out.println("Enter the Staff ID of the member you want to update:");
            String staffID = scanner.nextLine();

            boolean staffFound = false;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                Cell idCell = row.getCell(0);
                if (idCell != null && idCell.getStringCellValue().equalsIgnoreCase(staffID)) {
                    // Staff found, update details
                    System.out.println("Enter new name (current: " + row.getCell(1).getStringCellValue() + "): ");
                    String newName = scanner.nextLine();
                    row.getCell(1).setCellValue(newName);

                    // Validate role input
                    String newRole;
                    while (true) {
                        System.out.println("Enter new role (Doctor/Pharmacist/Administrator) (current: " + row.getCell(2).getStringCellValue() + "): ");
                        newRole = scanner.nextLine();
                        if (newRole.equalsIgnoreCase("Doctor") || newRole.equalsIgnoreCase("Pharmacist") || newRole.equalsIgnoreCase("Administrator")) {
                            break;  // Valid input
                        } else {
                            System.out.println("Invalid input. Please enter Doctor, Pharmacist, or Administrator.");
                        }
                    }
                    row.getCell(2).setCellValue(newRole);

                    // Validate gender input
                    String newGender;
                    while (true) {
                        System.out.println("Enter new gender (Male/Female/Others) (current: " + row.getCell(3).getStringCellValue() + "): ");
                        newGender = scanner.nextLine();
                        if (newGender.equalsIgnoreCase("Male") || newGender.equalsIgnoreCase("Female") || newGender.equalsIgnoreCase("Others")) {
                            break;  // Valid input
                        } else {
                            System.out.println("Invalid input. Please enter Male, Female, or Others.");
                        }
                    }
                    row.getCell(3).setCellValue(newGender);

                    // Validate age input
                    int newAge;
                    while (true) {
                        System.out.println("Enter new age (current: " + (int) row.getCell(4).getNumericCellValue() + "): ");
                        if (scanner.hasNextInt()) {
                            newAge = scanner.nextInt();
                            if (newAge >= 0) {
                                break;  // Valid input
                            } else {
                                System.out.println("Invalid input. Age must be a non-negative integer.");
                            }
                        } else {
                            System.out.println("Invalid input. Please enter a non-negative integer.");
                            scanner.next();  // Clear invalid input
                        }
                    }
                    scanner.nextLine();  // Consume newline after integer input
                    row.getCell(4).setCellValue(newAge);

                    staffFound = true;
                    break;
                }
            }

            if (!staffFound) {
                System.out.println("Staff member not found.");
            } else {
                // Write the updated data back to the Excel file
                FileOutputStream outputStream = new FileOutputStream(STAFF_FILE_PATH);
                workbook.write(outputStream);
                workbook.close();
                outputStream.close();
                file.close();
                System.out.println("Staff member updated successfully.");
            }
        }




        public void removeStaffMember(Scanner scanner) throws IOException {
            FileInputStream file = new FileInputStream(STAFF_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            System.out.println("Enter the Staff ID of the member you want to remove:");
            String staffID = scanner.nextLine();

            boolean staffFound = false;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {  // Start from row 1 to skip the header
                Row row = sheet.getRow(i);
                if (row == null) continue;  // Skip empty rows

                Cell idCell = row.getCell(0);
                if (idCell != null && idCell.getStringCellValue().equalsIgnoreCase(staffID)) {
                    // Staff found, remove by shifting remaining rows up
                    sheet.removeRow(row);
                    staffFound = true;
                    break;
                }
            }

            if (!staffFound) {
                System.out.println("Staff member not found.");
            } else {
                // Write the updated data back to the Excel file
                FileOutputStream outputStream = new FileOutputStream(STAFF_FILE_PATH);
                workbook.write(outputStream);
                workbook.close();
                outputStream.close();
                file.close();
                System.out.println("Staff member removed successfully.");
            }
        }
        
        public void approveReplenishmentRequests() throws IOException {
            // Open the Replenishment_List.xlsx file
            FileInputStream replenishmentFile = new FileInputStream(REPLENISHMENT_FILE_PATH);
            Workbook replenishmentWorkbook = new XSSFWorkbook(replenishmentFile);
            Sheet replenishmentSheet = replenishmentWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

            List<Row> pendingRequests = new ArrayList<>();

            // List all pending requests
            System.out.println("Pending Replenishment Requests:");
            boolean hasPendingRequests = false;
            int index = 1;

            for (Row row : replenishmentSheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String status = getCellValueAsString(row.getCell(3));  // Assuming Status is in column 4

                // Only show pending requests
                if (status.equalsIgnoreCase("pending")) {
                    hasPendingRequests = true;
                    pendingRequests.add(row);
                    String pharmacistID = getCellValueAsString(row.getCell(0));  // Pharmacist ID (Column 1)
                    String medicationName = getCellValueAsString(row.getCell(1));  // Medication Name (Column 2)
                    String requestedAmount = getCellValueAsString(row.getCell(2));  // Requested Amount (Column 3)

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
            String medicationName = getCellValueAsString(selectedRow.getCell(1));  // Medication Name (Column 2)
            int requestedAmount = Integer.parseInt(getCellValueAsString(selectedRow.getCell(2)));  // Requested Amount (Column 3)

            // Approve the selected request (Update the status in column 4)
            createOrUpdateCell(selectedRow, 3, "approved");  // Update Status to "approved"
            System.out.println("Replenishment request approved successfully.");

            // Write the updated status back to the Replenishment_List.xlsx file
            replenishmentFile.close();  // Close input stream
            FileOutputStream replenishmentOutputStream = new FileOutputStream(REPLENISHMENT_FILE_PATH);
            replenishmentWorkbook.write(replenishmentOutputStream);
            replenishmentWorkbook.close();
            replenishmentOutputStream.close();

            // Now update the Initial Stock in Medicine_List.xlsx
            updateMedicineStock(medicationName, requestedAmount);
        }

        // Method to update the Initial Stock in the Medicine_List.xlsx file
        private void updateMedicineStock(String medicationName, int addedAmount) throws IOException {
            // Open the Medicine_List.xlsx file
            FileInputStream medicineFile = new FileInputStream(MEDICINE_FILE_PATH);
            Workbook medicineWorkbook = new XSSFWorkbook(medicineFile);
            Sheet medicineSheet = medicineWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

            boolean medicationFound = false;

            // Iterate through rows to find the matching medication and update stock
            for (Row row : medicineSheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String currentMedicationName = getCellValueAsString(row.getCell(0));  // Medication Name in column 1
                if (currentMedicationName.equalsIgnoreCase(medicationName)) {
                    int currentStock = (int) row.getCell(1).getNumericCellValue();  // Current stock in column 2
                    row.getCell(1).setCellValue(currentStock + addedAmount);  // Update stock level
                    medicationFound = true;
                    System.out.println("Updated Stock for " + medicationName + ": " + (currentStock + addedAmount));
                    break;
                }
            }
            if (!medicationFound) {
                System.out.println("Medication not found in the inventory.");
            }

            // Write the updated stock back to the Medicine_List.xlsx file
            medicineFile.close();  // Close input stream
            FileOutputStream medicineOutputStream = new FileOutputStream(MEDICINE_FILE_PATH);
            medicineWorkbook.write(medicineOutputStream);
            medicineWorkbook.close();
            medicineOutputStream.close();
        }
        
        public void viewReplenishmentRequests(Pharmacist pharmacist) throws IOException {
            // Open the Replenishment_List.xlsx file
            FileInputStream file = new FileInputStream(REPLENISHMENT_FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

            boolean hasRequests = false;

            // Loop through each row and display the details of each replenishment request
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header row

                String pharmacistID = getCellValueAsString(row.getCell(0));  // Pharmacist ID (Column 1)
                String medicationName = getCellValueAsString(row.getCell(1));  // Medication Name (Column 2)
                String requestedAmount = getCellValueAsString(row.getCell(2));  // Requested Amount (Column 3)
                String status = getCellValueAsString(row.getCell(3));  // Status (Column 4)

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
                if (!isValidPassword(newPassword)) {
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
            
            public String findLastAppointmentID() throws IOException {
                FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

                String lastAppointmentID = "APT0";  // Start with a default value
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;  // Skip the header row

                    Cell appointmentIDCell = row.getCell(2);  // Assuming appointment ID is in column 3
                    if (appointmentIDCell != null) {
                        String currentID = appointmentIDCell.getStringCellValue();
                        if (currentID.startsWith("APT")) {
                            lastAppointmentID = currentID;  // Keep track of the last appointment ID
                        }
                    }
                }

                workbook.close();
                file.close();

                return lastAppointmentID;
            }
            
            public void viewMedicalRecords(Patient patient) throws IOException {
                FileInputStream file = new FileInputStream(PATIENT_FILE_PATH);
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains patient data

                boolean patientFound = false;

                // Iterate through the rows to find the patient's records
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;  // Skip the header row

                    String patientID = getCellValueAsString(row.getCell(0));  // Assuming patient ID is in column 1
                    if (patient.getHospitalID().equalsIgnoreCase(patientID)) {
                        patientFound = true;
                        
                        // Display patient details
                        System.out.println("Patient Name: " + getCellValueAsString(row.getCell(1)));  // Assuming name is in column 2
                        System.out.println("Date of Birth: " + getCellValueAsString(row.getCell(2)));  // Assuming DOB is in column 3
                        System.out.println("Gender: " + getCellValueAsString(row.getCell(3)));  // Assuming gender is in column 4
                        System.out.println("Blood Type: " + getCellValueAsString(row.getCell(4)));  // Assuming blood type is in column 5
                        System.out.println("Email: " + getCellValueAsString(row.getCell(5)));  // Assuming email is in column 6
                        System.out.println("Phone Number: " + getCellValueAsString(row.getCell(6)));  // Assuming phone number is in column 7

                        // Read and display past diagnoses
                        String pastDiagnoses = getCellValueAsString(row.getCell(7));  // Assuming past diagnoses are in column 8
                        if (pastDiagnoses.isEmpty()) {
                            System.out.println("Past Diagnoses: No past diagnoses found.");
                        } else {
                            System.out.println("Past Diagnoses: " + pastDiagnoses);
                        }
                        break;
                    }
                }

                if (!patientFound) {
                    System.out.println("Patient record not found.");
                }

                workbook.close();
                file.close();
            }
            
            public void handleOutstandingBills(Patient patient) throws IOException {
                FileInputStream file = new FileInputStream(APPOINTMENT_FILE_PATH);
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

                List<Row> outstandingAppointments = new ArrayList<>();
                System.out.println("Outstanding Bills for " + patient.getName() + ":");

                // Loop through the rows to find completed appointments
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;  // Skip header row

                    String patientID = getCellValueAsString(row.getCell(1));  // Assuming patient ID is in column 2
                    String status = getCellValueAsString(row.getCell(6));  // Assuming status is in column 7
                    
                    // If the patient ID matches and the appointment is completed
                    if (patient.getHospitalID().equalsIgnoreCase(patientID) && status.equalsIgnoreCase("completed")) {
                        outstandingAppointments.add(row);
                        String appointmentID = getCellValueAsString(row.getCell(2));  // Assuming appointment ID is in column 3
                        String date = getCellValueAsString(row.getCell(5));  // Assuming date/time is in column 6
                        System.out.println((outstandingAppointments.size()) + ". Appointment ID: " + appointmentID + " | Date/Time: " + date);
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
                FileOutputStream outputStream = new FileOutputStream(APPOINTMENT_FILE_PATH);
                workbook.write(outputStream);
                workbook.close();
                outputStream.close();
            }
            





        }






	



