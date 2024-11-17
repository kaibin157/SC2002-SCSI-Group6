package oop.util;

import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

/**
 * A utility class providing helper methods for password validation and Excel cell manipulation using Apache POI.
 */
public class Helper {

    /**
     * Validates a password based on the following criteria:
     * <ul>
     *     <li>At least 12 characters long</li>
     *     <li>Contains at least one uppercase letter</li>
     *     <li>Contains at least one lowercase letter</li>
     *     <li>Contains at least one digit</li>
     *     <li>Contains at least one special character</li>
     * </ul>
     *
     * @param password the password string to validate
     * @return {@code true} if the password meets all criteria; {@code false} otherwise
     */
    public static boolean isValidPassword(String password) {
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

    /**
     * Retrieves the value of a cell as a {@code String}, handling different cell types such as
     * string, numeric (including dates), and boolean.
     *
     * @param cell the {@link Cell} from which to retrieve the value
     * @return the cell value as a {@code String}; returns an empty string if the cell is null or of an unsupported type
     */
    public static String getCellValueAsString(Cell cell) {
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

    /**
     * Creates a new cell or updates an existing cell in a given row at the specified column index with the provided value.
     *
     * @param row         the {@link Row} in which to create or update the cell
     * @param columnIndex the zero-based index of the column where the cell is located
     * @param value       the {@code String} value to set in the cell
     */
    public static void createOrUpdateCell(Row row, int columnIndex, String value) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        cell.setCellValue(value);
    }
    
    /**
     * Gets a validated integer choice from the user within the specified range.
     *
     * @param scanner the {@link Scanner} object for reading user input
     * @param min     the minimum valid value (inclusive)
     * @param max     the maximum valid value (inclusive)
     * @return a validated integer choice within the range [min, max]
     */
    public static int getChoice(Scanner scanner, int min, int max) {
        int choice = -1;
        while (true) {
            System.out.print("Enter your choice (" + min + "-" + max + "): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline
                if (choice >= min && choice <= max) {
                    break;
                } else {
                    System.out.println("Invalid choice. Please enter a number between " + min + " and " + max + ".");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next(); // Clear invalid input
            }
        }
        return choice;
    }

}
