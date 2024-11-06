package oop.models;

import oop.HMS;

import java.util.Scanner;

public abstract class User {
    protected String hospitalID;
    protected String password;
    protected String name;

    public User(String hospitalID, String password, String name) {
        this.hospitalID = hospitalID;
        this.password = password;
        this.name = name;
    }

    public String getHospitalID() {
        return hospitalID;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getName() {
        return name;
    }



    public void changePassword() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter new password:");
        this.password = scanner.nextLine();
        System.out.println("Password changed successfully.");
    }

    public abstract void displayMenu(HMS hms);
}


