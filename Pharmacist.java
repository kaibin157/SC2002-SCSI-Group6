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
            System.out.println("6. Change Password");
            System.out.println("7. Logout");

            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

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
                case 6: 
				try {
					hms.changePassword(this, scanner);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                case 7:
                    return;  // Logout
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    
    

}

