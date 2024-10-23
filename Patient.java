import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;

public class Patient {
    private int patientID;
    private String name;
    private String dob;
    private String gender;
    private int phoneNumber;
    private String email;

    public Patient(int patientID, String name, String dob, String gender, int phoneNumber, String email) {
        this.patientID = patientID;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // Method to retrieve patient details by patientID from CSV
    public static Patient getPatientFromCSV(int searchPatientID, String csvFilePath) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            String[] nextLine;
            reader.readNext(); // Skip header row

            while ((nextLine = reader.readNext()) != null) {
                int patientID = Integer.parseInt(nextLine[0]);
                if (patientID == searchPatientID) {
                    String name = nextLine[1];
                    String dob = nextLine[2];
                    String gender = nextLine[3];
                    int phoneNumber = Integer.parseInt(nextLine[4]);
                    String email = nextLine[5];

                    // Return a new Patient object with the found data
                    return new Patient(patientID, name, dob, gender, phoneNumber, email);
                }
            }

            System.out.println("Patient with ID " + searchPatientID + " not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void viewMedicalRecord() {
        System.out.println("Patient Medical Record:");
        System.out.println("Patient ID: " + patientID);
        System.out.println("Name: " + name);
        System.out.println("Date of Birth: " + dob);
        System.out.println("Gender: " + gender);
    }

    public void viewContactInfo() {
        System.out.println("Patient Contact Information:");
        System.out.println("Patient ID: " + patientID);
        System.out.println("Phone Number: " + phoneNumber);
        System.out.println("Email: " + email);
    }

    // Main method for testing
    public static void main(String[] args) {
        Patient patient = Patient.getPatientFromCSV(1001, "patients.csv");
        if (patient != null) {
            patient.viewMedicalRecord();
            System.out.println("");
            patient.viewContactInfo();
        }
    }
}
