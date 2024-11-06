package oop.controller;

import oop.utils.Constant;
import oop.utils.Helper;
import oop.models.*;
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

public class UserController {

    private List<Doctor> doctors = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Pharmacist> pharmacists = new ArrayList<>();
    private List<Patient> patients = new ArrayList<>();

    public List<Doctor> getDoctors() {
        return doctors;
    }
    public List<User> getUsers(){ return users; }

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

    // Method to update patient information in the Excel file
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

    // Method to load patients from Excel file
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

    // Method for viewing patient records (doctor can only see their patients' records)
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
                String contactInfo = Helper.getCellValueAsString(row.getCell(5));  // Contact in column 6
                String pastDiagnoses = Helper.getCellValueAsString(row.getCell(6));  // Past diagnoses in column 7

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
    public void updatePatientRecords() throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter patient ID to update records:");
        String patientID = scanner.nextLine();

        // Open the Patient_List.xlsx file
        FileInputStream file = new FileInputStream(Constant.PATIENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming the data is on the first sheet

        boolean patientFound = false;

        // Iterate through rows in the sheet to find the patient by ID
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String excelPatientID = Helper.getCellValueAsString(row.getCell(0));  // Assuming patient ID is in column 1
            if (excelPatientID.equalsIgnoreCase(patientID)) {
                patientFound = true;

                // Retrieve the existing diagnoses from the "Past Diagnoses" column (assumed to be column 7)
                String pastDiagnoses = Helper.getCellValueAsString(row.getCell(6));

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
        FileOutputStream outputStream = new FileOutputStream(Constant.PATIENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    // Method for viewing doctor's personal schedule (appointments)
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

    // Method for setting doctor's availability for appointments
    public void setDoctorAvailability(Doctor doctor) throws IOException {
        Scanner scanner = new Scanner(System.in);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setLenient(false);  // Disable lenient parsing to ensure strict format

        // Open the Excel file for doctor availability
        FileInputStream availabilityFile = new FileInputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
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
                    FileOutputStream outputStream = new FileOutputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
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

}
