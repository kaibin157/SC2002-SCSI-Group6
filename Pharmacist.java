package oop;

import java.io.IOException;
import java.util.Scanner;

public class Pharmacist extends User {
    private String gender;
    private String age;

    public Pharmacist(String hospitalID, String password, String name, String gender, String age) {
        super(hospitalID, password, name);
        this.gender = gender;
        this.age = age;
    }
    
    // Getters for gender and age
    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }


    @Override
    public void displayMenu(HMS hms) {
        Scanner scanner = new Scanner(System.in);  // Create scanner inside the method

        while (true) {
            System.out.println("Pharmacist Menu:");
            System.out.println("1. View Appointment Outcome Record");
            System.out.println("2. Update Prescription Status");
            System.out.println("3. View Medication Inventory");
            System.out.println("4. Submit Replenishment Request");
            System.out.println("5. View Replenishment Requests");
            System.out.println("6. Send Invoice");
            System.out.println("7. Change Password");
            System.out.println("8. Logout");

            int choice = -1;

            // Validate input to ensure it's an integer within the valid range (1-11)
            while (true) {
                System.out.print("Enter your choice (1-8): ");
                
                // Check if input is an integer
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();  // Consume newline

                    // Check if choice is in the valid range
                    if (choice >= 1 && choice <= 8) {
                        break;  // Valid input
                    } else {
                        System.out.println("Invalid choice. Please enter a number between 1 and 8.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.next();  // Clear invalid input
                }
            }

            switch (choice) {
                case 1:
				try {
					hms.viewAppointmentOutcomeRecord();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    break;
                case 2:
				try {
					hms.updatePrescriptionStatus();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    break;
                case 3:
				try {
					hms.viewMedicationInventory();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    break;
                case 4:
				try {
					hms.submitReplenishmentRequest(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // Submit Replenishment Request
                    break;
                case 5:
				try {
					hms.viewReplenishmentRequests(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // View Replenishment Requests
                    break;
                case 6:  // New case for sending invoices
                    try {
                        hms.sendInvoice(this);  // Method to send invoice
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 7: 
				try {
					hms.changePassword(this, scanner);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                case 8:
                	System.out.println("Logging out...");
                    return;  // Logout
                default:
                    System.out.println("Unexpected error.");
            }
        }
    }

    
    

}

