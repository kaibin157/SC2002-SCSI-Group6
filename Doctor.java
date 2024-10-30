package oop;

import java.util.Scanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Doctor extends User {
    private String gender;
    private String age;
    private List<String> availability;  // List to store available dates and times (slots)

    public Doctor(String hospitalID, String password, String name, String gender, String age) {
        super(hospitalID, password, name);
        this.gender = gender;
        this.age = age;
        this.availability = new ArrayList<>();  // Initialize availability list
    }

    // Getters for gender and age
    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    // Method to set availability (add a new available slot)
    public void setAvailability(String availabilitySlot) {
        this.availability.add(availabilitySlot);
    }

    // Method to retrieve the availability
    public List<String> getAvailability() {
        return availability;
    }

    // Method to remove a booked slot from availability
    public void removeAvailability(String slot) {
        this.availability.remove(slot);  // Remove slot once it's booked
    }

    @Override
    public void displayMenu(HMS hms) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Doctor Menu:");
            System.out.println("1. View Patient Medical Records");
            System.out.println("2. Update Patient Medical Records");
            System.out.println("3. View Personal Schedule");
            System.out.println("4. Set Availability for Appointments");
            System.out.println("5. Accept or Decline Appointments");
            System.out.println("6. View Upcoming Appointments");
            System.out.println("7. Record Appointment Outcome");
            System.out.println("8. Change Password");  // New option to change password
            System.out.println("9. Logout");

            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            switch (choice) {
                case 1:
				try {
					hms.viewPatientRecords(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // Existing method
                    break;
                case 2:
				try {
					hms.updatePatientRecords(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // Existing method
                    break;
                case 3:
				try {
					hms.viewDoctorSchedule(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // Existing method
                    break;
                case 4:
				try {
					hms.setDoctorAvailability(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // Existing method for setting availability
                    break;
                case 5:
				try {
					hms.manageAppointmentRequests(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // Existing method to manage appointment requests
                    break;
                case 6:
				try {
					hms.viewUpcomingAppointments(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // View upcoming appointments
                    break;
                case 7:
				try {
					hms.recordAppointmentOutcome(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // Record appointment outcome
                    break;
                case 8:
				try {
					hms.changePassword(this, scanner);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // Call the password change method
                    break;
                case 9:
                    return;  // Logout
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
