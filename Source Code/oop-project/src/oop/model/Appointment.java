package oop.model;

import java.util.List;

/**
 * The Appointment class represents a medical appointment in the system,
 * containing details about the patient, doctor, date, status, and the outcome
 * of the appointment, such as services provided and prescribed medications.
 */
public class Appointment {

    /** Unique identifier for the appointment. */
    private String appointmentID;

    /** Patient associated with the appointment. */
    private Patient patient;

    /** Doctor assigned to the appointment. */
    private Doctor doctor;

    /** Date and time of the appointment. */
    private String dateTime;

    /** Current status of the appointment (e.g., Scheduled, Completed, Canceled). */
    private String status;

    /** Service provided during the appointment. */
    private String serviceProvided;

    /** Consultation notes from the appointment. */
    private String consultationNotes;

    /** List of medications prescribed during the appointment. */
    private List<Medication> prescribedMedications;

    /**
     * Constructs an Appointment with the specified details.
     *
     * @param appointmentID         The unique identifier for the appointment.
     * @param patient               The patient associated with the appointment.
     * @param doctor                The doctor assigned to the appointment.
     * @param dateTime              The date and time of the appointment.
     * @param status                The current status of the appointment.
     * @param prescribedMedications The list of medications prescribed during the appointment.
     */
    public Appointment(String appointmentID, Patient patient, Doctor doctor, String dateTime, String status, List<Medication> prescribedMedications) {
        this.appointmentID = appointmentID;
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.status = status;
        this.prescribedMedications = prescribedMedications;
    }

    /**
     * Returns the unique identifier for the appointment.
     *
     * @return The appointment ID.
     */
    public String getAppointmentID() {
        return appointmentID;
    }

    /**
     * Returns the patient associated with the appointment.
     *
     * @return The patient.
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * Returns the doctor assigned to the appointment.
     *
     * @return The doctor.
     */
    public Doctor getDoctor() {
        return doctor;
    }

    /**
     * Returns the date and time of the appointment.
     *
     * @return The appointment date and time.
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Sets the date and time of the appointment.
     *
     * @param dateTime The new date and time for the appointment.
     */
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Returns the current status of the appointment.
     *
     * @return The appointment status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of the appointment.
     *
     * @param status The new status of the appointment.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the service provided during the appointment.
     *
     * @return The service provided.
     */
    public String getServiceProvided() {
        return serviceProvided;
    }

    /**
     * Sets the service provided during the appointment.
     *
     * @param serviceProvided The service provided.
     */
    public void setServiceProvided(String serviceProvided) {
        this.serviceProvided = serviceProvided;
    }

    /**
     * Returns the consultation notes from the appointment.
     *
     * @return The consultation notes.
     */
    public String getConsultationNotes() {
        return consultationNotes;
    }

    /**
     * Sets the consultation notes for the appointment.
     *
     * @param consultationNotes The consultation notes.
     */
    public void setConsultationNotes(String consultationNotes) {
        this.consultationNotes = consultationNotes;
    }

    /**
     * Returns the list of medications prescribed during the appointment.
     *
     * @return The list of prescribed medications.
     */
    public List<Medication> getPrescribedMedications() {
        return prescribedMedications;
    }

    /**
     * Sets the list of medications prescribed during the appointment.
     *
     * @param prescribedMedications The list of prescribed medications.
     */
    public void setPrescribedMedications(List<Medication> prescribedMedications) {
        this.prescribedMedications = prescribedMedications;
    }
}
