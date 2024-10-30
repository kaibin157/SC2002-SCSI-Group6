package oop;

import java.io.IOException;
import java.util.Scanner;

public class Administrator extends User {
    private String gender;
    private String age;

    public Administrator(String hospitalID, String password, String name,String gender, String age) {
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

    
    public void displayMenu(HMS hms) {
        Scanner scanner = new Scanner(System.in);  // Scanner instantiated inside the method

        while (true) {
            System.out.println("Administrator Menu:");
            System.out.println("1. View and Manage Hospital Staff");
            System.out.println("2. View Appointments Details");
            System.out.println("3. Manage Medication Inventory");
            System.out.println("4. Approve Replenishment Requests");
            System.out.println("5. Change Password");
            System.out.println("6. Logout");

            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            try {
                switch (choice) {
                    case 1:
                        hms.manageHospitalStaff();
                        break;
                    case 2:
                        hms.viewAppointmentDetails();
                        break;
                    case 3:
                        hms.manageMedicationInventory(scanner);
                        break;
                    case 4:
                        hms.approveReplenishmentRequests();  // Approve Replenishment Requests
                        break;
                    case 5: 
        				try {
        					hms.changePassword(this, scanner);
        				} catch (IOException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
                    case 6:
                        return;  // Logout
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }


}

