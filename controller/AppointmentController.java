package oop.controller;

import oop.models.Appointment;
import oop.models.Doctor;
import oop.models.Patient;
import oop.utils.Constant;
import oop.utils.Helper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class AppointmentController {

    private int nextAppointmentNumber;
    private List<Appointment> appointments = new ArrayList<>();

    public AppointmentController() {
        String lastAppointmentID = null;
        try {
            lastAppointmentID = findLastAppointmentID();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        nextAppointmentNumber = Integer.parseInt(lastAppointmentID.substring(3)) + 1;  // Get the number and increment it    }
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }
    // 2. View Appointment Details
    public void viewAppointmentDetails() throws IOException {
        // Open the Appointment_List.xlsx file
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains appointment data

        System.out.println("Appointment Details:");

        // Loop through each row in the sheet
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            // Retrieve the necessary details from the respective columns
            String patientID = Helper.getCellValueAsString(row.getCell(1));  // Assuming Patient ID is in column 2
            String doctorID = Helper.getCellValueAsString(row.getCell(0));  // Assuming Doctor ID is in column 1
            String status = Helper.getCellValueAsString(row.getCell(6));    // Assuming Status is in column 7
            String appointmentDate = Helper.getCellValueAsString(row.getCell(5));  // Assuming Date is in column 6

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

    public void viewAvailableAppointmentSlots() throws IOException {
        FileInputStream file = new FileInputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains doctor availability data

        System.out.println("Available Appointment Slots:");

        // Iterate through the rows to list available slots
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip the header row

            // Read the doctor ID and available date/time slot
            String doctorID = Helper.getCellValueAsString(row.getCell(0));  // Assuming doctor ID is in column 1
            String appointmentDate = Helper.getCellValueAsString(row.getCell(1));  // Assuming appointment slot is in column 2

            // Display available slots
            System.out.println("Doctor ID: " + doctorID + " | Available Slot: " + appointmentDate);
        }

        workbook.close();
        file.close();
    }

    // Method to record the new appointment in Appointment_List.xlsx
    public void recordAppointmentInExcel(Appointment appointment) throws IOException {

        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
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
        FileOutputStream outputStream = new FileOutputStream(Constant.APPOINTMENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        file.close();

    }

    // 2. Schedule an appointment for a patient
    public void scheduleAppointment(Patient patient, List<Doctor> doctors) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Open the Excel file to read doctor availability
        FileInputStream file = new FileInputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
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
            Doctor doctor = findDoctorByID(doctorID,doctors);

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

    // 3. Reschedule an appointment for a patient
    public void rescheduleAppointment(Patient patient) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Open Appointment_List.xlsx to fetch current appointments
        FileInputStream appointmentFile = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook appointmentWorkbook = new XSSFWorkbook(appointmentFile);
        Sheet appointmentSheet = appointmentWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

        List<Row> patientAppointments = new ArrayList<>();

        // Display the patient's current appointments
        System.out.println("Your scheduled appointments:");
        boolean hasAppointments = false;
        for (Row row : appointmentSheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String patientID = Helper.getCellValueAsString(row.getCell(1));  // Assuming patient ID is in column 2
            String status = Helper.getCellValueAsString(row.getCell(6));     // Assuming status is in column 7

            if (patient.getHospitalID().equalsIgnoreCase(patientID) && (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("confirmed"))) {              hasAppointments = true;
                patientAppointments.add(row);
                String appointmentID = Helper.getCellValueAsString(row.getCell(2));  // Assuming appointment ID is in column 3
                String dateTime = Helper.getCellValueAsString(row.getCell(5));       // Assuming date/time is in column 6
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
        String oldDateTime = Helper.getCellValueAsString(selectedAppointmentRow.getCell(5));  // Assuming old slot in column 6
        String doctorID = Helper.getCellValueAsString(selectedAppointmentRow.getCell(0));     // Assuming doctor ID in column 1

        // Show available slots for the selected doctor from DocAvailability_List.xlsx
        FileInputStream availabilityFile = new FileInputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
        Workbook availabilityWorkbook = new XSSFWorkbook(availabilityFile);
        Sheet availabilitySheet = availabilityWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

        List<String> availableSlots = new ArrayList<>();
        List<Row> availableRows = new ArrayList<>();

        System.out.println("Available slots for Doctor ID: " + doctorID);
        for (Row row : availabilitySheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String availableDoctorID = Helper.getCellValueAsString(row.getCell(0));  // Assuming Doctor ID in column 1
            if (availableDoctorID.equalsIgnoreCase(doctorID)) {
                String availabilitySlot = Helper.getCellValueAsString(row.getCell(1));  // Assuming availability slot in column 2
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
        FileOutputStream availabilityOutputStream = new FileOutputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
        availabilityWorkbook.write(availabilityOutputStream);
        availabilityWorkbook.close();
        availabilityOutputStream.close();

        FileOutputStream appointmentOutputStream = new FileOutputStream(Constant.APPOINTMENT_FILE_PATH);
        appointmentWorkbook.write(appointmentOutputStream);
        appointmentWorkbook.close();
        appointmentOutputStream.close();

        System.out.println("Appointment rescheduled successfully to " + newDateTime);
    }

    // 4. Cancel an appointment for a patient
    public void cancelAppointment(Patient patient) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Open the Appointment_List.xlsx file
        FileInputStream appointmentFile = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook appointmentWorkbook = new XSSFWorkbook(appointmentFile);
        Sheet appointmentSheet = appointmentWorkbook.getSheetAt(0);  // Assuming the first sheet

        List<Row> cancelableAppointments = new ArrayList<>();

        // Display only "pending" or "confirmed" appointments for the patient
        System.out.println("Your pending or confirmed appointments:");
        boolean hasCancelableAppointments = false;

        for (Row row : appointmentSheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String patientID = Helper.getCellValueAsString(row.getCell(1));  // Assuming Patient ID is in column 2
            String status = Helper.getCellValueAsString(row.getCell(6));  // Assuming Status is in column 7
            String appointmentID = Helper.getCellValueAsString(row.getCell(2));  // Assuming Appointment ID is in column 3
            String dateTime = Helper.getCellValueAsString(row.getCell(5));  // Assuming Date/Time is in column 6

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
        String doctorID = Helper.getCellValueAsString(selectedAppointmentRow.getCell(0));  // Assuming Doctor ID is in column 1
        String appointmentSlot = Helper.getCellValueAsString(selectedAppointmentRow.getCell(5));  // Assuming Date/Time is in column 6

        // Cancel the appointment by setting the status to "cancelled"
        selectedAppointmentRow.getCell(6).setCellValue("cancelled");  // Update status to "cancelled" in column 7

        // Write the updated status back to the Appointment_List.xlsx file
        appointmentFile.close();  // Close input stream before writing
        FileOutputStream appointmentOutStream = new FileOutputStream(Constant.APPOINTMENT_FILE_PATH);
        appointmentWorkbook.write(appointmentOutStream);
        appointmentWorkbook.close();
        appointmentOutStream.close();

        // Add the appointment slot back to the doctor's availability in DocAvailability_List.xlsx
        if (doctorID != null && appointmentSlot != null) {
            addSlotBackToDoctorAvailability(doctorID, appointmentSlot);
        }

        System.out.println("Appointment cancelled successfully.");
    }

    // 5. View scheduled appointments for a patient
    public void viewScheduledAppointments(Patient patient) throws IOException {
        System.out.println("Your scheduled appointments:");

        // Open the Excel file to read the appointment list
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
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

    public void viewAppointmentOutcomeRecord() throws IOException {
        // Open the Appointment_List.xlsx file
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains appointment data

        System.out.println("Viewing appointment outcome records...");

        // Loop through the rows in the sheet
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip the header row

            // Retrieve necessary information from each row
            String appointmentID = Helper.getCellValueAsString(row.getCell(2));  // Appointment ID (Column 3)
            String patientName = Helper.getCellValueAsString(row.getCell(4));    // Patient Name (Column 1)
            String doctorName = Helper.getCellValueAsString(row.getCell(3));     // Doctor Name (Column 4)
            String date = Helper.getCellValueAsString(row.getCell(5));           // Appointment Date (Column 6)
            String medications = Helper.getCellValueAsString(row.getCell(9));    // Prescribed Medications (Column 5)
            String status = Helper.getCellValueAsString(row.getCell(6));

            // Display the information
            if (status.equalsIgnoreCase("completed") || status.equalsIgnoreCase("paid") || status.equalsIgnoreCase("unpaid")) {
                System.out.println("Appointment ID: " + appointmentID);
                System.out.println("Patient Name: " + patientName);
                System.out.println("Doctor Name: " + doctorName);
                System.out.println("Date: " + date);
                System.out.println("Status: " + status);
                System.out.println("Prescribed Medications: " + medications);
                System.out.println("------------------------------------");
            }

        }

        workbook.close();
        file.close();
    }

    // Method to add slot back to DocAvailability_List.xlsx
    private void addSlotBackToDoctorAvailability(String doctorID, String appointmentSlot) throws IOException {
        FileInputStream availabilityFile = new FileInputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
        Workbook availabilityWorkbook = new XSSFWorkbook(availabilityFile);
        Sheet availabilitySheet = availabilityWorkbook.getSheetAt(0);  // Assuming first sheet

        // Add the appointment slot back to the availability list for the doctor
        int lastRowNum = availabilitySheet.getLastRowNum();
        Row newRow = availabilitySheet.createRow(lastRowNum + 1);

        newRow.createCell(0).setCellValue(doctorID);  // Doctor ID in column 1
        newRow.createCell(1).setCellValue(appointmentSlot);  // Appointment slot in column 2

        // Write changes to DocAvailability_List.xlsx
        FileOutputStream availabilityOutStream = new FileOutputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
        availabilityWorkbook.write(availabilityOutStream);
        availabilityWorkbook.close();
        availabilityOutStream.close();
        availabilityFile.close();
    }

    public void viewUpcomingAppointments(Doctor doctor) throws IOException {
        // Open the Appointment_List.xlsx file
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
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
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains appointment data

        // Display all confirmed appointments for the doctor
        System.out.println("Confirmed Appointments for Dr. " + doctor.getName() + ":");
        boolean hasConfirmedAppointments = false;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String appointmentID = Helper.getCellValueAsString(row.getCell(2));  // Assuming Appointment ID is in column 3
            String doctorID = Helper.getCellValueAsString(row.getCell(0));  // Assuming Doctor ID is in column 2
            String status = Helper.getCellValueAsString(row.getCell(6));  // Assuming Status is in column 7

            if (doctor.getHospitalID().equals(doctorID) && status.equalsIgnoreCase("confirmed")) {
                hasConfirmedAppointments = true;
                System.out.println("Appointment ID: " + appointmentID + " | Date/Time: " + Helper.getCellValueAsString(row.getCell(5)));

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
                    Helper.createOrUpdateCell(row, 7, serviceProvided);  // Column 4: Service Provided
                    Helper.createOrUpdateCell(row, 9, prescribedMedicationsInput);  // Column 5: Prescribed Medications
                    Helper.createOrUpdateCell(row, 8, consultationNotes);  // Column 9: Consultation Notes
                    Helper.createOrUpdateCell(row, 6, "completed");  // Column 7: Set appointment status to 'completed'
                    if (!prescribedMedicationsInput.equalsIgnoreCase("nil")) {
                        Helper.createOrUpdateCell(row, 10, "pending");  // Column 10: Prescription Status
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
        FileOutputStream outputStream = new FileOutputStream(Constant.APPOINTMENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public void viewPastAppointmentOutcomes(Patient patient) throws IOException {
        // Open the Appointment_List.xlsx file
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains appointment data

        System.out.println("Past Appointment Outcomes for " + patient.getName() + ":");

        boolean hasPastAppointments = false;  // Flag to track if there are any past completed appointments

        // Loop through the rows in the Excel sheet
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip the header row

            // Read the patient ID from the second column (assuming patient ID is in column 1)
            String patientID = Helper.getCellValueAsString(row.getCell(1));

            // Check if the appointment belongs to the logged-in patient and if the status is "completed"
            String status = Helper.getCellValueAsString(row.getCell(6));  // Assuming status is in column 7
            if (patient.getHospitalID().equalsIgnoreCase(patientID) && (status.equalsIgnoreCase("completed") || status.equalsIgnoreCase("paid"))) {
                hasPastAppointments = true;

                // Retrieve and display the appointment details
                String doctorName = Helper.getCellValueAsString(row.getCell(3));  // Assuming doctor name is in column 4
                String date = Helper.getCellValueAsString(row.getCell(5));         // Assuming date is in column 6
                String serviceProvided = Helper.getCellValueAsString(row.getCell(7));  // Assuming service provided is in column 8
                String prescribedMedications = Helper.getCellValueAsString(row.getCell(9));  // Assuming medications are in column 10
                String consultationNotes = Helper.getCellValueAsString(row.getCell(8));  // Assuming consultation notes are in column 9


                // Display the past appointment details
                System.out.println("Doctor: " + doctorName);
                System.out.println("Date: " + date);
                System.out.println("Service Provided: " + serviceProvided);
                System.out.println("Prescribed Medications: " + prescribedMedications);
                System.out.println("Consultation Notes: " + consultationNotes);
                System.out.println("Status: " + status);


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

    public String findLastAppointmentID() throws IOException {
        FileInputStream file = new FileInputStream(Constant.APPOINTMENT_FILE_PATH);
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
    
    // Helper method to remove a doctor's availability from Excel after booking
    // Helper method to remove a doctor's availability from Excel after booking
    private void removeDoctorAvailabilityFromExcel(String doctorID, String slot) throws IOException {
        FileInputStream file = new FileInputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
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
            FileOutputStream outputStream = new FileOutputStream(Constant.DOC_AVAILABILITY_FILE_PATH);
            workbook.write(outputStream);
            outputStream.close();
        }

        workbook.close();
        file.close();
    }

    // Helper method to generate unique appointment IDs
    private String generateAppointmentID() {
        String newAppointmentID = "APT" + nextAppointmentNumber;
        nextAppointmentNumber++;  // Increment for the next appointment
        return newAppointmentID;
    }

    // Helper method to find a doctor by ID from the list of doctors
    private Doctor findDoctorByID(String doctorID, List<Doctor> doctors) {
        for (Doctor doctor : doctors) {
            if (doctor.getHospitalID().equalsIgnoreCase(doctorID)) {
                return doctor;
            }
        }
        return null;
    }
}

