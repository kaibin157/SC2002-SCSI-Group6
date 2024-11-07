package oop.model;

import oop.HMS;
import java.util.Scanner;

/**
 * Represents a generic user in the hospital management system.
 * This is an abstract class that defines common attributes and methods for all users.
 */
public abstract class User {

    protected String hospitalID;
    protected String password;
    protected String name;

    /**
     * Constructs a User with the specified hospital ID, password, and name.
     *
     * @param hospitalID the unique ID of the user within the hospital system
     * @param password the user's password
     * @param name the name of the user
     */
    public User(String hospitalID, String password, String name) {
        this.hospitalID = hospitalID;
        this.password = password;
        this.name = name;
    }

    /**
     * Gets the hospital ID of the user.
     *
     * @return the hospital ID of the user
     */
    public String getHospitalID() {
        return hospitalID;
    }

    /**
     * Gets the password of the user.
     *
     * @return the password of the user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets a new password for the user.
     *
     * @param password the new password to be set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the name of the user.
     *
     * @return the name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * Prompts the user to enter a new password and updates the password accordingly.
     * Displays a success message once the password is changed.
     */
    public void changePassword() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter new password:");
        this.password = scanner.nextLine();
        System.out.println("Password changed successfully.");
    }

    /**
     * Displays the menu for the specific type of user.
     * This is an abstract method that must be implemented by subclasses to define user-specific menus.
     *
     * @param hms the hospital management system instance used to display the menu
     */
    public abstract void displayMenu(HMS hms);
}
