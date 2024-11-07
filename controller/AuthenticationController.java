package oop.controller;

import oop.model.AuthManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The AuthenticationController class implements the AuthManager interface
 * and manages authentication details from an Excel file. It allows for
 * authenticating users and updating passwords in both in-memory data and
 * the Excel file.
 */
public class AuthenticationController implements AuthManager {
    /** Stores the authentication data with hospital IDs as keys and passwords as values. */
    private Map<String, String> authenticationMap;

    /** File path of the Excel file storing authentication data. */
    private String authFilePath;

    /**
     * Constructor to initialize the AuthenticationController with a specified Excel file path.
     * Loads the authentication data from the Excel file into memory.
     *
     * @param authFilePath The file path of the Excel file storing authentication data.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    public AuthenticationController(String authFilePath) throws IOException {
        this.authFilePath = authFilePath;
        authenticationMap = new HashMap<>();
        loadAuthenticationData();
    }

    /**
     * Loads authentication data from the Excel file into the authenticationMap.
     *
     * @throws IOException if an I/O error occurs while reading the Excel file.
     */
    private void loadAuthenticationData() throws IOException {
        FileInputStream file = new FileInputStream(authFilePath);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String staffID = row.getCell(0).getStringCellValue();
            String password = row.getCell(1).getStringCellValue();
            authenticationMap.put(staffID, password);
        }

        workbook.close();
        file.close();
    }

    /**
     * Authenticates a user by checking if the provided hospitalID and password
     * match a record in the Excel file.
     *
     * @param hospitalID The hospital ID to authenticate.
     * @param password   The password associated with the hospital ID.
     * @return true if authentication is successful; false otherwise.
     * @throws IOException if an I/O error occurs while reading the Excel file.
     */
    @Override
    public boolean authenticate(String hospitalID, String password) throws IOException {
        FileInputStream authFile = new FileInputStream(authFilePath);
        Workbook authWorkbook = new XSSFWorkbook(authFile);
        Sheet authSheet = authWorkbook.getSheetAt(0);  // Assuming data is on the first sheet

        for (Row row : authSheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            String storedID = row.getCell(0).getStringCellValue();  // Read Hospital ID
            String storedPassword = row.getCell(1).getStringCellValue();  // Read Password

            // Check if hospitalID and password match the stored values
            if (storedID.equals(hospitalID) && storedPassword.equals(password)) {
                authWorkbook.close();
                authFile.close();
                return true;  // Successful authentication
            }
        }

        authWorkbook.close();
        authFile.close();
        return false;  // Authentication failed
    }

    /**
     * Updates the password for a specified hospital ID in both the in-memory map and the Excel file.
     *
     * @param hospitalID  The hospital ID for which to update the password.
     * @param newPassword The new password to set for the hospital ID.
     * @throws IOException if an I/O error occurs while updating the Excel file.
     */
    @Override
    public void updatePassword(String hospitalID, String newPassword) throws IOException {
        // Update the in-memory map
        authenticationMap.put(hospitalID, newPassword);

        // Now update the Excel file
        FileInputStream file = new FileInputStream(authFilePath);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header row

            Cell idCell = row.getCell(0);
            if (idCell.getStringCellValue().equals(hospitalID)) {
                row.getCell(1).setCellValue(newPassword);  // Update password in the Excel row
                break;
            }
        }

        // Write the updated workbook back to the file
        FileOutputStream outputStream = new FileOutputStream(authFilePath);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        file.close();
    }
}
