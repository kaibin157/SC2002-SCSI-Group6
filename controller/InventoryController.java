package oop.controller;

import oop.model.Medication;
import oop.model.Patient;
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
import java.util.*;

/**
 * The {@code InventoryController} class manages the inventory of medications within the system.
 * It provides functionalities to add, update, remove medications, view inventory, and handle prescription statuses.
 * This class interacts with Excel files to persist and retrieve medication data.
 */
public class InventoryController {

    private List<Medication> inventory = new ArrayList<>();

    /**
     * Adds a new medication to the inventory by collecting details from the user and updating the Excel file.
     *
     * @param scanner the {@link Scanner} object used to read user input
     * @param sheet   the {@link Sheet} object representing the medication data in Excel
     * @param workbook the {@link Workbook} object representing the Excel workbook
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
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
        FileOutputStream outputStream = new FileOutputStream(Constant.MEDICINE_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();

        System.out.println("Medication added successfully.");
    }

    /**
     * Updates the stock level of an existing medication by collecting new stock information from the user.
     *
     * @param scanner the {@link Scanner} object used to read user input
     * @param sheet   the {@link Sheet} object representing the medication data in Excel
     * @param workbook the {@link Workbook} object representing the Excel workbook
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
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
            FileOutputStream outputStream = new FileOutputStream(Constant.MEDICINE_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            System.out.println("Stock level updated successfully.");
        }
    }

    /**
     * Removes a medication from the inventory based on the medication name provided by the user.
     *
     * @param scanner the {@link Scanner} object used to read user input
     * @param sheet   the {@link Sheet} object representing the medication data in Excel
     * @param workbook the {@link Workbook} object representing the Excel workbook
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
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
            FileOutputStream outputStream = new FileOutputStream(Constant.MEDICINE_FILE_PATH);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            System.out.println("Medication removed successfully.");
        }
    }

    /**
     * Updates the stock level of a medication by adding a specified amount.
     *
     * @param medicationName the name of the medication to update
     * @param addedAmount    the amount to add to the current stock
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void updateMedicineStock(String medicationName, int addedAmount) throws IOException {
        // Open the Medicine_List.xlsx file
        FileInputStream medicineFile = new FileInputStream(Constant.MEDICINE_FILE_PATH);
        Workbook medicineWorkbook = new XSSFWorkbook(medicineFile);
        Sheet medicineSheet = medicineWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

        boolean medicationFound = false;

        // Iterate through rows to find the matching medication and update stock
        for (Row row : medicineSheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String currentMedicationName = Helper.getCellValueAsString(row.getCell(0));  // Medication Name in column 1
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
        FileOutputStream medicineOutputStream = new FileOutputStream(Constant.MEDICINE_FILE_PATH);
        medicineWorkbook.write(medicineOutputStream);
        medicineWorkbook.close();
        medicineOutputStream.close();
    }

    /**
     * Views the medical records of a specific patient by reading from the patient Excel file.
     *
     * @param patient the {@link Patient} whose medical records are to be viewed
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void viewMedicalRecords(Patient patient) throws IOException {
        FileInputStream file = new FileInputStream(Constant.PATIENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains patient data

        boolean patientFound = false;

        // Iterate through the rows to find the patient's records
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip the header row

            String patientID = Helper.getCellValueAsString(row.getCell(0));  // Assuming patient ID is in column 1
            if (patient.getHospitalID().equalsIgnoreCase(patientID)) {
                patientFound = true;

                // Display patient details
                System.out.println("Patient Name: " + Helper.getCellValueAsString(row.getCell(1)));  // Assuming name is in column 2
                System.out.println("Date of Birth: " + Helper.getCellValueAsString(row.getCell(2)));  // Assuming DOB is in column 3
                System.out.println("Gender: " + Helper.getCellValueAsString(row.getCell(3)));  // Assuming gender is in column 4
                System.out.println("Blood Type: " + Helper.getCellValueAsString(row.getCell(4)));  // Assuming blood type is in column 5
                System.out.println("Email: " + Helper.getCellValueAsString(row.getCell(5)));  // Assuming email is in column 6
                System.out.println("Phone Number: " + Helper.getCellValueAsString(row.getCell(6)));  // Assuming phone number is in column 7

                // Read and display past diagnoses
                String pastDiagnoses = Helper.getCellValueAsString(row.getCell(7));  // Assuming past diagnoses are in column 8
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

    /**
     * Updates the prescription status of medications from "pending" to "dispensed" based on user input.
     *
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void updatePrescriptionStatus() throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Open the Excel file
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        List<Row> pendingAppointments = new ArrayList<>();

        // List all appointments with prescription status "pending"
        System.out.println("Pending Prescriptions to be dispensed:");
        boolean hasPendingPrescriptions = false;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String prescriptionStatus = Helper.getCellValueAsString(row.getCell(10));  // Assuming Prescription Status is in column 11

            if (prescriptionStatus.equalsIgnoreCase("pending")) {
                hasPendingPrescriptions = true;
                pendingAppointments.add(row);  // Add the row to the list of pending appointments

                String appointmentID = Helper.getCellValueAsString(row.getCell(2));  // Assuming Appointment ID is in column 3
                String patientName = Helper.getCellValueAsString(row.getCell(1));    // Assuming Patient Name is in column 2
                String dateTime = Helper.getCellValueAsString(row.getCell(5));       // Assuming Date/Time is in column 6
                String meds = Helper.getCellValueAsString(row.getCell(9));          // Assuming Medications are in column 10
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
        Helper.createOrUpdateCell(selectedRow, 10, "dispensed");  // Update Prescription Status to "dispensed"
        System.out.println("Prescription status updated to 'dispensed'.");

        // Write changes back to the Excel file
        file.close();
        FileOutputStream outputStream = new FileOutputStream(Constant.APPOINTMENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    /**
     * Views the medication inventory by reading and displaying data from the Excel file.
     *
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void viewMedicationInventory() throws IOException {
        FileInputStream file = new FileInputStream(Constant.MEDICINE_FILE_PATH);
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

    /**
     * Saves the current medication inventory to the Excel file by updating stock levels.
     *
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void saveMedicationsToExcel() throws IOException {
        FileInputStream file = new FileInputStream(Constant.MEDICINE_FILE_PATH);
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

        FileOutputStream outFile = new FileOutputStream(Constant.MEDICINE_FILE_PATH);
        workbook.write(outFile);
        workbook.close();
        outFile.close();
        file.close();
    }

    /**
     * Loads medications from the Excel file into the inventory list.
     *
     * @throws IOException if an I/O error occurs while accessing the Excel file
     */
    public void loadMedicationsFromExcel() throws IOException {
        FileInputStream file = new FileInputStream(Constant.MEDICINE_FILE_PATH);
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

    /**
     * Searches for a medication in the inventory by its name.
     *
     * @param medicationName the name of the medication to search for
     * @return the {@link Medication} object if found; {@code null} otherwise
     */
    public Medication getMedicationByName(String medicationName) {
        for (Medication medication : inventory) {
            if (medication.getName().equalsIgnoreCase(medicationName)) {
                return medication;  // Medication found
            }
        }
        return null;  // Medication not found
    }
}
