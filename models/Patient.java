package oop.models;

import oop.HMS;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class Patient extends User {
    private String dateOfBirth;
    private String gender;
    private String email;  // Separate field for email
    private String phoneNumber;  // Separate field for phone number
    private String bloodType;
    private List<String> pastDiagnoses;  // List to store past diagnoses

    
    public Patient(String hospitalID, String password, String name, String dateOfBirth, String gender, String email, String phoneNumber, String bloodType) {
        super(hospitalID, password, name);  // Call the User constructor
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.bloodType = bloodType;
        this.pastDiagnoses = new ArrayList<>();  // Initialize as an empty list
    }
    
    

    // Getters for the fields
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBloodType() {
        return bloodType;
    }

    
    // Getter for past diagnoses
    public List<String> getPastDiagnoses() {
        return pastDiagnoses;
    }

    // Method to add a diagnosis to the patient's record
    public void addDiagnosis(String diagnosis) {
        this.pastDiagnoses.add(diagnosis);
    }
    




    @Override
    public void displayMenu(HMS hms) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Patient Menu:");
            System.out.println("1. View Medical Record");
            System.out.println("2. Update Personal Information");
            System.out.println("3. View Available Appointment Slots");
            System.out.println("4. Schedule an Appointment");
            System.out.println("5. Reschedule an Appointment");
            System.out.println("6. Cancel an Appointment");
            System.out.println("7. View Scheduled Appointments");
            System.out.println("8. View Past Appointment Outcome Records");
            System.out.println("9. Pay Outstanding Bills");
            System.out.println("10. Change Password");
            System.out.println("11. Logout");

            int choice = -1;

            // Validate input to ensure it's an integer within the valid range (1-11)
            while (true) {
                System.out.print("Enter your choice (1-11): ");
                
                // Check if input is an integer
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();  // Consume newline

                    // Check if choice is in the valid range
                    if (choice >= 1 && choice <= 11) {
                        break;  // Valid input
                    } else {
                        System.out.println("Invalid choice. Please enter a number between 1 and 11.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    scanner.next();  // Clear invalid input
                }
            }

            switch (choice) {
                case 1:
				try {
					hms.viewMedicalRecords(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
                    break;
                case 2:
                    updatePersonalInfo(scanner,hms);
                    break;
                case 3:
				try {
					hms.viewAvailableAppointmentSlots();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    break;
                case 4:
				try {
					hms.scheduleAppointment(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    break;
                case 5:
				try {
					hms.rescheduleAppointment(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    break;
                case 6:
				try {
					hms.cancelAppointment(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    break;
                case 7:
				try {
					hms.viewScheduledAppointments(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    break;
                case 8:
				try {
					hms.viewPastAppointmentOutcomes(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // Call method to view past appointments
                    break;
                case 9:
                    try {
                        hms.handleOutstandingBills(this);  // New method for payment system
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 10:
				try {
					hms.changePassword(this, scanner);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                case 11:
                	System.out.println("Logging out...");
                    return;  // Logout
                default:
                    System.out.println("Unexpected error.");
            }
        }
    }



    public void updatePersonalInfo(Scanner scanner, HMS hms) {
        while (true) {
            System.out.println("What would you like to update?");
            System.out.println("1. Email");
            System.out.println("2. Phone Number");
            System.out.println("Enter your choice (1 or 2):");

            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                // Updating email
                while (true) {
                    System.out.println("Enter your new email address:");
                    String newEmail = scanner.nextLine();

                    // Basic email validation using regex
                    if (newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                        this.setEmail(newEmail);  // Update email in memory
                        System.out.println("Email updated successfully.");
                        break;
                    } else {
                        System.out.println("Invalid email format. Please enter a valid email.");
                    }
                }
                break;
            } else if (choice.equals("2")) {
                // Updating phone number
                while (true) {
                    System.out.println("Enter your new phone number (digits only):");
                    String newPhoneNumber = scanner.nextLine();

                    // Basic phone number validation (only digits allowed, minimum 5 digits)
                    if (newPhoneNumber.matches("\\d{5,20}")) {
                        this.setPhoneNumber(newPhoneNumber);  // Update phone number in memory
                        System.out.println("Phone number updated successfully.");
                        break;
                    } else {
                        System.out.println("Invalid phone number. Please enter a valid phone number.");
                    }
                }
                break;
            } else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }

        // Now update the information in the Excel file
        try {
            hms.updatePatientInfoInExcel(this);  // Call HMS method to update Excel file
        } catch (IOException e) {
            System.out.println("Error updating patient information in Excel: " + e.getMessage());
        }
    }

}



