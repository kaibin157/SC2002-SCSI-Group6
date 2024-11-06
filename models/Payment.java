package oop.models;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Payment {
	// Encryption key for AES-256
	private static final String AES_KEY = "0123456789abcdef0123456789abcdef"; 
	private static final String PATIENT_FILE_PATH = "Patient_List.xlsx";
	
    public Payment() {
    }
    
    public void processPayment(Row appointmentRow, Patient patient, Scanner scanner) throws IOException {
        System.out.println("Proceed to payment for Appointment ID: " + getCellValueAsString(appointmentRow.getCell(2)));

        // Check if the patient already has saved card details
        String savedCardDetails = getSavedCardDetailsForPatient(patient);

        String cardNumber = null;
        String expiryDate = null;

        if (savedCardDetails != null) {
            // Decrypt the saved card details
            String[] decryptedCardDetails = decryptCardDetailsAES(savedCardDetails);
            cardNumber = decryptedCardDetails[0];
            expiryDate = decryptedCardDetails[1];

            System.out.println("Using saved card details for payment.");
            System.out.println("Card Number: **** **** **** " + cardNumber.substring(12));  // Mask all but the last 4 digits
            System.out.println("Expiry Date: " + expiryDate);
        } else {
            // Card Number Input
            while (true) {
                System.out.print("Enter your card number (16 digits): ");
                cardNumber = scanner.nextLine().replaceAll("\\s", "");  // Remove spaces
                if (cardNumber.matches("\\d{16}")) {
                    break;  // Valid card number
                } else {
                    System.out.println("Invalid card number. Please enter exactly 16 digits.");
                }
            }

            // Expiry Date Input
            while (true) {
                System.out.print("Enter card expiry date (MM/YY): ");
                expiryDate = scanner.nextLine();
                if (expiryDate.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
                    // Check if expiry date is in the future
                    if (isExpiryDateValid(expiryDate)) {
                        break;
                    } else {
                        System.out.println("Card expiry date must be in the future.");
                    }
                } else {
                    System.out.println("Invalid expiry date. Please enter in MM/YY format.");
                }
            }
        }

        // CVC Input (still required even if card details are saved)
        String cvc;
        while (true) {
            System.out.print("Enter card CVC (3 digits): ");
            cvc = scanner.nextLine();
            if (cvc.matches("\\d{3}")) {
                break;  // Valid CVC
            } else {
                System.out.println("Invalid CVC. Please enter exactly 3 digits.");
            }
        }

        // Payment successful
        System.out.println("Payment successful. Thank you!");

        // If the card wasn't saved before, ask to save it
        if (savedCardDetails == null) {
            System.out.print("Do you want to save your card details for future payments? (yes/no): ");
            String saveCardResponse = scanner.nextLine().trim().toLowerCase();

            if (saveCardResponse.equals("yes")) {
                // Encrypt card details using AES-256
                String encryptedCardDetails = encryptCardDetailsAES(cardNumber, expiryDate);

                // Save encrypted card details in the 9th column of Patient_List.xlsx for the current patient
                saveCardDetailsForPatient(patient, encryptedCardDetails);
                System.out.println("Card details saved successfully.");
            }
        }
    }

    // Method to get saved card details for a patient
    private String getSavedCardDetailsForPatient(Patient patient) throws IOException {
        FileInputStream file = new FileInputStream(PATIENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        // Find the patient's row in the sheet
        for (Row row : sheet) {
            String patientID = getCellValueAsString(row.getCell(0));  // Assuming Patient ID is in the first column
            if (patient.getHospitalID().equalsIgnoreCase(patientID)) {
                // Check if the patient has saved card details in the 9th column (index 8)
                Cell cardDetailsCell = row.getCell(8);
                if (cardDetailsCell != null) {
                    return cardDetailsCell.getStringCellValue();  // Return encrypted card details
                }
                break;
            }
        }

        workbook.close();
        file.close();
        return null;  // No card details saved
    }

 
    private String[] decryptCardDetailsAES(String encryptedCardDetails) {
        try {
            // Split the encrypted string into IV and encrypted data
            String[] parts = encryptedCardDetails.split(":");
            if (parts.length != 2) {
                throw new RuntimeException("Invalid encrypted data format");
            }

            byte[] iv = Base64.getDecoder().decode(parts[0]);  // Extract IV
            byte[] encryptedData = Base64.getDecoder().decode(parts[1]);  // Extract encrypted data

            byte[] key = AES_KEY.getBytes("UTF-8");  // Use the same predefined AES key

            // Create AES cipher instance
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            // Initialize cipher in decryption mode
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // Decrypt the card details
            byte[] decryptedData = cipher.doFinal(encryptedData);
            String decryptedString = new String(decryptedData);

            // Split the decrypted string into card number and expiry date
            return decryptedString.split("\\|");
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card details", e);
        }
    }








 
    private String encryptCardDetailsAES(String cardNumber, String expiryDate) {
        try {
            byte[] key = AES_KEY.getBytes("UTF-8");  // Use a predefined AES key

            // Create AES Cipher instance
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Generate random IV (Initialization Vector)
            byte[] iv = new byte[16];  // AES block size is 16 bytes
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            // Initialize cipher in encryption mode
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // Combine card number and expiry date
            String cardDetails = cardNumber + "|" + expiryDate;

            // Encrypt the card details
            byte[] encrypted = cipher.doFinal(cardDetails.getBytes());

            // Encode IV + encrypted text to Base64 and return (IV is needed for decryption)
            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card details", e);
        }
    }







    // Method to generate a 256-bit AES key
    private byte[] generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);  // Initialize key generator for 256-bit AES key
            SecretKey secretKey = keyGen.generateKey();
            return secretKey.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Error generating AES key", e);
        }
    }

    // Method to save encrypted card details for a patient in Patient_List.xlsx
    private void saveCardDetailsForPatient(Patient patient, String encryptedCardDetails) throws IOException {
        FileInputStream file = new FileInputStream(PATIENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);  // Assuming data is on the first sheet

        // Find the patient's row in the sheet
        for (Row row : sheet) {
            String patientID = getCellValueAsString(row.getCell(0));  // Assuming Patient ID is in the first column
            if (patient.getHospitalID().equalsIgnoreCase(patientID)) {
                // Save encrypted card details in the 9th column (index 8)
                Cell cardDetailsCell = row.getCell(8);
                if (cardDetailsCell == null) {
                    cardDetailsCell = row.createCell(8);
                }
                cardDetailsCell.setCellValue(encryptedCardDetails);
                break;
            }
        }

        // Save the updated workbook
        FileOutputStream outputStream = new FileOutputStream(PATIENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        file.close();
    }

    // Method to validate expiry date (must be in the future)
    private boolean isExpiryDateValid(String expiryDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yy");
            dateFormat.setLenient(false);
            Date expiry = dateFormat.parse(expiryDate);
            return expiry.after(new Date());  // Check if expiry date is in the future
        } catch (ParseException e) {
            return false;  // Invalid date format
        }
    }
    
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
}
