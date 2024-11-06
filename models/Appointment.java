package oop.models;

import java.util.List;

public class Appointment {

    // Basic Appointment Details
    private String appointmentID;
    private Patient patient;
    private Doctor doctor;
    private String dateTime;
    private String status;

    // Fields to store appointment outcome
    private String serviceProvided;
    private String consultationNotes;
    private List<Medication> prescribedMedications;  // List of prescribed medications

    // Constructor to initialize an appointment
    public Appointment(String appointmentID, Patient patient, Doctor doctor, String dateTime, String status, List<Medication> prescribedMedications) {
        this.appointmentID = appointmentID;
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.status = status;
        this.prescribedMedications = prescribedMedications;
    }

    // Getters and Setters for Basic Appointment Details

    public String getAppointmentID() {
        return appointmentID;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public String getDateTime() {
        return dateTime;
    }
    
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getters and Setters for Appointment Outcome Details

    public String getServiceProvided() {
        return serviceProvided;
    }

    public void setServiceProvided(String serviceProvided) {
        this.serviceProvided = serviceProvided;
    }

    public String getConsultationNotes() {
        return consultationNotes;
    }

    public void setConsultationNotes(String consultationNotes) {
        this.consultationNotes = consultationNotes;
    }

    public List<Medication> getPrescribedMedications() {
        return prescribedMedications;
    }

    public void setPrescribedMedications(List<Medication> prescribedMedications) {
        this.prescribedMedications = prescribedMedications;
    }
}
