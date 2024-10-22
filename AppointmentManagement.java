public class AppointmentManagement {
    private int patientID;
    private int doctorID;
    private String appointmentStatus;
    private String date;
    private String time;
    private boolean appointmentOutcome;

    //* Can use polyamrism 
    public AppointmentManagement  (int patientID, int doctorID, String appointmentStatus, String date, String time, boolean appointmentOutcome) {
        this.patientID = patientID;
        this.doctorID = doctorID;
        this.appointmentStatus = appointmentStatus;
        this.date = date;
        this.time = time;
        this.appointmentOutcome = appointmentOutcome;
    } 

    public int getPatientID() {
        return patientID;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public boolean getAppointmentOutcome() {
        return appointmentOutcome;
    }
}
