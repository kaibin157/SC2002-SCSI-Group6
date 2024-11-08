package oop.controller;

import oop.model.*;
import oop.util.Constant;
import oop.util.Helper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * The {@code UserController} class manages user-related operations within the hospital system,
 * including handling hospital staff, patient records, and doctor schedules. It interacts with
 * various Excel files to persist and retrieve user and staff data.
 */
public class UserController {

    private List<Doctor> doctors = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Pharmacist> pharmacists = new ArrayList<>();
    private List<Patient> patients = new ArrayList<>();

    /**
     * Retrieves the list of all doctors.
     *
     * @return a {@link List} of {@link Doctor} objects
     */
    public List<Doctor> getDoctors() {
        return doctors;
    }

    /**
     * Retrieves the list of all users.
     *
     * @return a {@link List} of {@link User} objects
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Displays the list of hospital staff by reading from the staff Excel file.
     *
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void viewHospitalStaff() throws IOException {
        FileInputStream file = new FileInputStream(Constant.STAFF_FILE_PATH);
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

    /**
     * Adds a new staff member to the hospital by collecting details from the user and updating the Excel files.
     *
     * @param scanner the {@link Scanner} object used to read user input
     * @throws IOException if an I/O error occurs while accessing the Excel files
     */
    public void addStaffMember(Scanner scanner) throws IOException {
        // Open the Excel file for staff
        FileInputStream staffFile = new FileInputStream(Constant.STAFF_FILE_PATH);
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
        FileOutputStream staffOutputStream = new FileOutputStream(Constant.STAFF_FILE_PATH);
        staffWorkbook.write(staffOutputStream);
        staffWorkbook.close();
        staffOutputStream.close();
        staffFile.close();

        // Now append the new staff's authentication details to Authentication_List.xlsx
        appendAuthenticationToExcel(staffID);

        System.out.println("Staff member added successfully.");
    }

    /**
     * Updates the details of an existing staff member by collecting new information from the user and updating the Excel file.
     *
     * @param scanner the {@link Scanner} object used to read user input
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void updateStaffMember(Scanner scanner) throws IOException {
        FileInputStream file = new FileInputStream(Constant.STAFF_FILE_PATH);
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
            FileOutputStream outputStream = new FileOutputStream(Constant.STAFF_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            file.close();
            System.out.println("Staff member updated successfully.");
        }
    }

    /**
     * Removes a staff member from the hospital by their Staff ID and updates the Excel file accordingly.
     *
     * @param scanner the {@link Scanner} object used to read user input
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void removeStaffMember(Scanner scanner) throws IOException {
        FileInputStream file = new FileInputStream(Constant.STAFF_FILE_PATH);
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
            FileOutputStream outputStream = new FileOutputStream(Constant.STAFF_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            file.close();
            System.out.println("Staff member removed successfully.");
        }
    }

    /**
     * Updates the email and phone number of a specific patient in the patient Excel file.
     *
     * @param patient the {@link Patient} whose information is to be updated
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void updatePatientInfoInExcel(Patient patient) throws IOException {
        FileInputStream file = new FileInputStream(Constant.PATIENT_FILE_PATH);
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
        FileOutputStream outputStream = new FileOutputStream(Constant.PATIENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        file.close();
    }

    /**
     * Loads staff members from the staff Excel file into the system's internal lists.
     *
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void loadStaffFromExcel() throws IOException {
        FileInputStream file = new FileInputStream(Constant.STAFF_FILE_PATH);  // Path to your staff Excel file
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip the header row

            // Map Excel columns to variables
            String hospitalID = Helper.getCellValueAsString(row.getCell(0));  // Staff ID (Column 1)
            String name = Helper.getCellValueAsString(row.getCell(1));        // Name (Column 2)
            String role = Helper.getCellValueAsString(row.getCell(2));        // Role (Column 3)
            String gender = Helper.getCellValueAsString(row.getCell(3));      // Gender (Column 4)
            String age = Helper.getCellValueAsString(row.getCell(4));         // Age (Column 5)

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

    /**
     * Appends authentication details for a new staff member to the authentication Excel file.
     *
     * @param staffID the ID of the staff member to append authentication for
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    private void appendAuthenticationToExcel(String staffID) throws IOException {
        // Open the Authentication_List.xlsx file
        FileInputStream authFile = new FileInputStream(Constant.AUTHENTICATION_FILE_PATH);
        Workbook authWorkbook = new XSSFWorkbook(authFile);
        Sheet authSheet = authWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

        // Create a new row at the end of the sheet
        int lastRowNum = authSheet.getLastRowNum();
        Row newRow = authSheet.createRow(lastRowNum + 1);

        // Add staff ID and default password
        newRow.createCell(0).setCellValue(staffID);      // Staff ID
        newRow.createCell(1).setCellValue("password");   // Default password

        // Write back to the Authentication_List.xlsx file
        FileOutputStream authOutputStream = new FileOutputStream(Constant.AUTHENTICATION_FILE_PATH);
        authWorkbook.write(authOutputStream);
        authWorkbook.close();
        authOutputStream.close();
        authFile.close();
    }

    /**
     * Loads patient records from the patient Excel file into the system's internal lists.
     *
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void loadPatientsFromExcel() throws IOException {
        FileInputStream file = new FileInputStream(Constant.PATIENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip the header row

            String hospitalID = Helper.getCellValueAsString(row.getCell(0));  // Patient ID
            String name = Helper.getCellValueAsString(row.getCell(1));        // Name
            String dob = Helper.getCellValueAsString(row.getCell(2));         // Date of Birth
            String gender = Helper.getCellValueAsString(row.getCell(3));      // Gender
            String bloodType = Helper.getCellValueAsString(row.getCell(4));   // Blood Type
            String email = Helper.getCellValueAsString(row.getCell(5));       // Email
            String phoneNumber = Helper.getCellValueAsString(row.getCell(6)); // Phone Number

            // Create a new patient object and add it to the system
            Patient patient = new Patient(hospitalID, "password", name, dob, gender, email, phoneNumber, bloodType);
            patients.add(patient);
            users.add(patient);  // Make sure to add the patient to the 'users' list
        }

        workbook.close();
        file.close();
    }

    /**
     * Views the medical records of a specific patient by reading from the patient Excel file.
     *
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void viewPatientRecords() throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter patient ID to view records:");
        String patientID = scanner.nextLine();

        // Open the Patient_List.xlsx file
        FileInputStream file = new FileInputStream(Constant.PATIENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming the data is on the first sheet

        boolean patientFound = false;

        // Iterate through rows in the sheet to find the patient by ID
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip the header row

            String excelPatientID = Helper.getCellValueAsString(row.getCell(0));  // Assuming patient ID is in column 1
            if (excelPatientID.equalsIgnoreCase(patientID)) {
                patientFound = true;

                // Retrieve patient details from the respective columns
                String name = Helper.getCellValueAsString(row.getCell(1));  // Name in column 2
                String dob = Helper.getCellValueAsString(row.getCell(2));   // Date of Birth in column 3
                String gender = Helper.getCellValueAsString(row.getCell(3));  // Gender in column 4
                String bloodType = Helper.getCellValueAsString(row.getCell(4));  // Blood Type in column 5
                String email = Helper.getCellValueAsString(row.getCell(5));  // email in column 6
                String phoneNumber = Helper.getCellValueAsString(row.getCell(6));
                String pastDiagnoses = Helper.getCellValueAsString(row.getCell(7));  // Past diagnoses in column 7
                String pastTreatments = Helper.getCellValueAsString(row.getCell(8));

                // Display patient details
                System.out.println("Patient Name: " + name);
                System.out.println("Date of Birth: " + dob);
                System.out.println("Gender: " + gender);
                System.out.println("Blood Type: " + bloodType);
                System.out.println("Email: " + email);
                System.out.println("Phone Number: " + phoneNumber);

                // Display past diagnoses
                if (pastDiagnoses == null || pastDiagnoses.isEmpty()) {
                    System.out.println("Past Diagnoses: No past diagnoses found.");
                } else {
                    System.out.println("Past Diagnoses: " + pastDiagnoses);
                }
                if (pastTreatments == null || pastTreatments.isEmpty()) {
                    System.out.println("Past Treatments: No past treatments found.");
                } else {
                    System.out.println("Past Treatments: " + pastTreatments);
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

    /**
     * Updates the medical records of a specific patient by adding a new diagnosis and updating the Excel file.
     *
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void updatePatientRecords() throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter patient ID to update records:");
        String patientID = scanner.nextLine();

        // Open the Patient_List.xlsx file
        FileInputStream file = new FileInputStream(Constant.PATIENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0); // Assuming the data is on the first sheet

        boolean patientFound = false;

        // Iterate through rows in the sheet to find the patient by ID
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip the header row

            String excelPatientID = Helper.getCellValueAsString(row.getCell(0)); // Assuming patient ID is in column 1
            if (excelPatientID.equalsIgnoreCase(patientID)) {
                patientFound = true;

                System.out.println("Choose an option:");
                System.out.println("1. Add New Diagnosis");
                System.out.println("2. Add New Treatment");
                int choice = Helper.getChoice(scanner, 1, 2);

                if (choice == 1) {
                    // Update Diagnoses
                    String pastDiagnoses = Helper.getCellValueAsString(row.getCell(7)); // Assuming column 7 is 'Past Diagnoses'

                    System.out.println("Enter new diagnosis:");
                    String newDiagnosis = scanner.nextLine();

                    // Append the new diagnosis to the existing diagnoses
                    if (pastDiagnoses == null || pastDiagnoses.isEmpty()) {
                        pastDiagnoses = newDiagnosis; // No previous diagnoses, just add the new one
                    } else {
                        pastDiagnoses = pastDiagnoses + ", " + newDiagnosis; // Append the new diagnosis to the existing ones
                    }

                    // Update the "Past Diagnoses" column
                    Cell diagnosisCell = row.getCell(7); // Assuming column 7 is the 'Past Diagnoses' column
                    if (diagnosisCell == null) {
                        diagnosisCell = row.createCell(7);
                    }
                    diagnosisCell.setCellValue(pastDiagnoses);

                    System.out.println("Diagnosis updated successfully.");
                } else if (choice == 2) {
                    // Update Treatments
                    String pastTreatments = Helper.getCellValueAsString(row.getCell(8)); // Assuming column 8 is 'Past Treatments'

                    System.out.println("Enter new treatment:");
                    String newTreatment = scanner.nextLine();

                    // Append the new treatment to the existing treatments
                    if (pastTreatments == null || pastTreatments.isEmpty()) {
                        pastTreatments = newTreatment; // No previous treatments, just add the new one
                    } else {
                        pastTreatments = pastTreatments + ", " + newTreatment; // Append the new treatment to the existing ones
                    }

                    // Update the "Past Treatments" column
                    Cell treatmentCell = row.getCell(8); // Assuming column 8 is the 'Past Treatments' column
                    if (treatmentCell == null) {
                        treatmentCell = row.createCell(8);
                    }
                    treatmentCell.setCellValue(pastTreatments);

                    System.out.println("Treatment updated successfully.");
                }
                break;
            }
        }

        if (!patientFound) {
            System.out.println("Patient not found.");
        }

        // Write the updated data back to the Excel file
        file.close();
        FileOutputStream outputStream = new FileOutputStream(Constant.PATIENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }


    /**
     * Views the schedule of a specific doctor by displaying their upcoming appointments and available time slots.
     *
     * @param doctor      the {@link Doctor} whose schedule is to be viewed
     * @param appointments a {@link List} of {@link Appointment} objects representing all appointments in the system
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void viewDoctorSchedule(Doctor doctor, List<Appointment> appointments) throws IOException {
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
        FileInputStream file = new FileInputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
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

    /**
     * Sets the availability of a doctor by allowing them to input available time slots,
     * validates the input, and updates the Excel file accordingly.
     *
     * @param doctor the {@link Doctor} whose availability is to be set
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void setDoctorAvailability(Doctor doctor) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Open the Excel file for doctor availability
        FileInputStream availabilityFile = new FileInputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
        Workbook availabilityWorkbook = new XSSFWorkbook(availabilityFile);
        Sheet availabilitySheet = availabilityWorkbook.getSheetAt(0); // Assuming data is on the first sheet

        while (true) {
            System.out.println("Enter available time slot for appointments (format: yyyy-MM-dd HH:mm): ");
            String input = scanner.nextLine();

            // Validate the input using the isValidDateTime method
            if (!isValidDateTime(input)) {
                System.out.println("Invalid format or date is in the past. Please follow the format: yyyy-MM-dd HH:mm and ensure the date is in the future.");
                continue;
            }

            // Check if the time slot is already in the doctor's availability list
            if (doctor.getAvailability().contains(input)) {
                System.out.println("This time slot has already been set. Please choose a different time.");
                continue;
            }

            // If valid, add it to the doctor's availability
            doctor.setAvailability(input);
            System.out.println("Availability set for: " + input);

            // Write the availability to the Excel file
            int lastRowNum = availabilitySheet.getLastRowNum();
            Row newRow = availabilitySheet.createRow(lastRowNum + 1);

            newRow.createCell(0).setCellValue(doctor.getHospitalID()); // Doctor ID
            newRow.createCell(1).setCellValue(input); // Availability slot

            // Save the changes to the Excel file
            FileOutputStream outputStream = new FileOutputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
            availabilityWorkbook.write(outputStream);
            outputStream.close();

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
    
    /**
     * Updates or deletes a doctor's availability in the system.
     *
     * @param doctor  the {@link Doctor} whose availability is to be updated
     * @param scanner the {@link Scanner} object used to read user input
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void updateDoctorAvailability(Doctor doctor, Scanner scanner) throws IOException {
        FileInputStream file = new FileInputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        System.out.println("Your Current Availability:");
        List<Row> doctorRows = new ArrayList<>();
        int index = 1;

        // Display current availability slots for the doctor
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip the header row

            String doctorID = Helper.getCellValueAsString(row.getCell(0));
            if (doctorID.equals(doctor.getHospitalID())) {
                String slot = Helper.getCellValueAsString(row.getCell(1));
                System.out.println(index + ". " + slot);
                doctorRows.add(row);
                index++;
            }
        }

        if (doctorRows.isEmpty()) {
            System.out.println("You currently have no availability set.");
            workbook.close();
            file.close();
            return;
        }

        System.out.println("\nOptions:");
        System.out.println("1. Change Time Slot");
        System.out.println("2. Delete Time Slot");
        int choice = Helper.getChoice(scanner, 1, 2);

        if (choice == 1) {
            System.out.print("Enter the number of the time slot to change: ");
            int slotChoice = Helper.getChoice(scanner, 1, doctorRows.size());
            Row selectedRow = doctorRows.get(slotChoice - 1);

            String newTiming;
            while (true) {
                System.out.print("Enter the new date and time (format: yyyy-MM-dd HH:mm): ");
                newTiming = scanner.nextLine();
                if (!isValidDateTime(newTiming)) {
                    System.out.println("Invalid format or date is in the past. Please enter a valid future date and time.");
                } else if (doctor.getAvailability().contains(newTiming)) {
                    System.out.println("This time slot has already been set. Please choose a different time.");
                } else {
                    break; // Valid input
                }
            }

            Helper.createOrUpdateCell(selectedRow, 1, newTiming); // Update the slot timing
            System.out.println("Time slot updated successfully.");
        } else if (choice == 2) {
            System.out.print("Enter the number of the time slot to delete: ");
            int slotChoice = Helper.getChoice(scanner, 1, doctorRows.size());
            Row selectedRow = doctorRows.get(slotChoice - 1);

            int rowIndex = selectedRow.getRowNum();
            sheet.removeRow(selectedRow);

            // Shift rows upward to remove empty space
            for (int i = rowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row rowToShift = sheet.getRow(i);
                Row newRow = sheet.createRow(i - 1);
                for (int j = 0; j < rowToShift.getLastCellNum(); j++) {
                    Cell oldCell = rowToShift.getCell(j);
                    if (oldCell != null) {
                        Helper.createOrUpdateCell(newRow, j, Helper.getCellValueAsString(oldCell));
                    }
                }
                sheet.removeRow(rowToShift);
            }

            System.out.println("Time Slot deleted successfully.");
        }

        file.close();
        FileOutputStream outputStream = new FileOutputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    
    /**
     * Validates if the given date and time string is in the correct format.
     *
     * @param dateTime the date and time string to validate
     * @return true if the string is valid; false otherwise
     */
    private boolean isValidDateTime(String dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setLenient(false); // Strict parsing
        try {
            Date parsedDate = dateFormat.parse(dateTime);
            return !parsedDate.before(new Date()); // Ensure the date is not in the past
        } catch (ParseException e) {
            return false;
        }
    }

}
