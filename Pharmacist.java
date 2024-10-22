package assignment;

class Pharmacist extends User {
    private String name;

    public Pharmacist(String hospitalID, String password, String name) {
        super(hospitalID, password);
        this.name = name;
    }

    public void menu() {
        // Pharmacist menu implementation (manage prescriptions and inventory)
        System.out.println("Pharmacist Menu:");
        // Add options here
    }

    // Methods for managing prescriptions
}

