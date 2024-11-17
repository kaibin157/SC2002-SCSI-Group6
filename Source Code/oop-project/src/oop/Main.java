package oop;

import oop.model.User;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize the hospital management system with authentication file
            HMS hms = new HMS("Authentication_List.xlsx");
            hms.initializeSystem();  // Load staff, patients, and medications from Excel files
            
            Scanner scanner = new Scanner(System.in);
            System.out.println("Welcome to the Hospital Management System !");
            System.out.println("  *****  ");
            System.out.println(" *     * ");
            System.out.println("*  O O  *");
            System.out.println("*   ^   *");
            System.out.println(" * \\_/ * ");
            System.out.println("  *****  ");
            while (true) {
                System.out.println("Enter your Hospital ID:");
                String hospitalID = scanner.nextLine().trim();// Remove leading and trailing sapces
                System.out.println("Enter your password:");
                String password = scanner.nextLine();

                // Attempt to log in
                User user = hms.login(hospitalID, password, scanner);  // Now returns a User object
                if (user != null) {
                    System.out.println("Login successful! Welcome, " + user.getName());
                    user.displayMenu(hms);  // Display the appropriate menu for the user
                    // Don't break the loop here; let it continue for the next login after the user logs out.
                } else if (!password.equals("password")) {  // Only show invalid credentials when not handling password change
                    System.out.println("Invalid credentials. Try again.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error initializing system: " + e.getMessage());
        }
    }
}
