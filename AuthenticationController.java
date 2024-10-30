package oop;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationController implements AuthManager {
    private Map<String, String> authenticationMap;
    private String authFilePath;  // Store the file path

    // Constructor to load the authentication data from Excel
    public AuthenticationController(String authFilePath) throws IOException {
        this.authFilePath = authFilePath;
        authenticationMap = new HashMap<>();
        loadAuthenticationData();
    }

    // Method to load authentication data from Excel
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

    // Implement the authenticate method from the AuthManager interface
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
                return true;  // Successful authentication
            }
        }

        authWorkbook.close();
        authFile.close();

        return false;  // Authentication failed
    }

    // Implement the updatePassword method from the AuthManager interface
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
