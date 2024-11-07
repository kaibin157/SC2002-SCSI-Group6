package oop.model;

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

/**
 * The Payment class handles processing patient payments,
 * saving and retrieving encrypted card details, and storing
 * this data in an Excel file.
 */
public class Payment {
    // Encryption key for AES-256
    private static final String AES_KEY = "0123456789abcdef0123456789abcdef";
    private static final String PATIENT_FILE_PATH = "Patient_List.xlsx";

    /**
     * Default constructor for Payment class.
     */
    public Payment() {
    }

    /**
     * Processes the payment for a patient's appointment.
     * Allows the use of saved card details if available and offers the option to save card details.
     *
     * @param appointmentRow The Excel row containing appointment data.
     * @param patient The Patient object containing patient information.
     * @param scanner Scanner object for user input.
     * @throws IOException If there's an issue accessing or modifying the Excel file.
     */
    public void processPayment(Row appointmentRow, Patient patient, Scanner scanner) throws IOException {
        System.out.println("Proceed to payment for Appointment ID: " + getCellValueAsString(appointmentRow.getCell(2)));

        String savedCardDetails = getSavedCardDetailsForPatient(patient);

        String cardNumber = null;
        String expiryDate = null;

        if (savedCardDetails != null) {
            // Decrypt saved card details
            String[] decryptedCardDetails = decryptCardDetailsAES(savedCardDetails);
            cardNumber = decryptedCardDetails[0];
            expiryDate = decryptedCardDetails[1];

            System.out.println("Using saved card details for payment.");
            System.out.println("Card Number: **** **** **** " + cardNumber.substring(12));
            System.out.println("Expiry Date: " + expiryDate);
        } else {
            // Get card number input from user
            while (true) {
                System.out.print("Enter your card number (16 digits): ");
                cardNumber = scanner.nextLine().replaceAll("\\s", "");
                if (cardNumber.matches("\\d{16}")) {
                    break;
                } else {
                    System.out.println("Invalid card number. Please enter exactly 16 digits.");
                }
            }

            // Get expiry date input from user
            while (true) {
                System.out.print("Enter card expiry date (MM/YY): ");
                expiryDate = scanner.nextLine();
                if (expiryDate.matches("(0[1-9]|1[0-2])/[0-9]{2}") && isExpiryDateValid(expiryDate)) {
                    break;
                } else {
                    System.out.println("Invalid expiry date. Please enter in MM/YY format.");
                }
            }
        }

        // Get CVC input from user
        String cvc;
        while (true) {
            System.out.print("Enter card CVC (3 digits): ");
            cvc = scanner.nextLine();
            if (cvc.matches("\\d{3}")) {
                break;
            } else {
                System.out.println("Invalid CVC. Please enter exactly 3 digits.");
            }
        }

        System.out.println("Payment successful. Thank you!");

        if (savedCardDetails == null) {
            System.out.print("Do you want to save your card details for future payments? (yes/no): ");
            String saveCardResponse = scanner.nextLine().trim().toLowerCase();

            if (saveCardResponse.equals("yes")) {
                String encryptedCardDetails = encryptCardDetailsAES(cardNumber, expiryDate);
                saveCardDetailsForPatient(patient, encryptedCardDetails);
                System.out.println("Card details saved successfully.");
            }
        }
    }

    /**
     * Retrieves saved encrypted card details for a given patient from the Excel file.
     *
     * @param patient The Patient object containing patient information.
     * @return The encrypted card details as a string, or null if no details are found.
     * @throws IOException If there's an issue accessing the Excel file.
     */
    private String getSavedCardDetailsForPatient(Patient patient) throws IOException {
        FileInputStream file = new FileInputStream(PATIENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            String patientID = getCellValueAsString(row.getCell(0));
            if (patient.getHospitalID().equalsIgnoreCase(patientID)) {
                Cell cardDetailsCell = row.getCell(8);
                if (cardDetailsCell != null) {
                    workbook.close();
                    file.close();
                    return cardDetailsCell.getStringCellValue();
                }
                break;
            }
        }

        workbook.close();
        file.close();
        return null;
    }

    /**
     * Decrypts the encrypted card details using AES-256 and returns the decrypted card number and expiry date.
     *
     * @param encryptedCardDetails The encrypted card details.
     * @return A string array containing the card number and expiry date.
     */
    private String[] decryptCardDetailsAES(String encryptedCardDetails) {
        try {
            String[] parts = encryptedCardDetails.split(":");
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encryptedData = Base64.getDecoder().decode(parts[1]);

            byte[] key = AES_KEY.getBytes("UTF-8");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData).split("\\|");
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card details", e);
        }
    }

    /**
     * Encrypts the card number and expiry date using AES-256.
     *
     * @param cardNumber The card number to encrypt.
     * @param expiryDate The expiry date to encrypt.
     * @return The encrypted card details as a Base64 encoded string.
     */
    private String encryptCardDetailsAES(String cardNumber, String expiryDate) {
        try {
            byte[] key = AES_KEY.getBytes("UTF-8");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            String cardDetails = cardNumber + "|" + expiryDate;

            byte[] encrypted = cipher.doFinal(cardDetails.getBytes());

            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card details", e);
        }
    }

    /**
     * Saves encrypted card details for a patient in the Patient_List.xlsx file.
     *
     * @param patient The Patient object containing patient information.
     * @param encryptedCardDetails The encrypted card details to save.
     * @throws IOException If there's an issue accessing or modifying the Excel file.
     */
    private void saveCardDetailsForPatient(Patient patient, String encryptedCardDetails) throws IOException {
        FileInputStream file = new FileInputStream(PATIENT_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            String patientID = getCellValueAsString(row.getCell(0));
            if (patient.getHospitalID().equalsIgnoreCase(patientID)) {
                Cell cardDetailsCell = row.getCell(8);
                if (cardDetailsCell == null) {
                    cardDetailsCell = row.createCell(8);
                }
                cardDetailsCell.setCellValue(encryptedCardDetails);
                break;
            }
        }

        FileOutputStream outputStream = new FileOutputStream(PATIENT_FILE_PATH);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        file.close();
    }

    /**
     * Validates if the expiry date is in the future.
     *
     * @param expiryDate The expiry date in MM/YY format.
     * @return True if the expiry date is valid and in the future, false otherwise.
     */
    private boolean isExpiryDateValid(String expiryDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yy");
            dateFormat.setLenient(false);
            Date expiry = dateFormat.parse(expiryDate);
            return expiry.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Retrieves the cell value as a string.
     *
     * @param cell The cell to retrieve the value from.
     * @return The cell value as a string.
     */
    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    return dateFormat.format(cell.getDateCellValue());
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
