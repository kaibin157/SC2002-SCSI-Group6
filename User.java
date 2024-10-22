package assignment;

abstract class User {
    protected String hospitalID;
    protected String password;

    public User(String hospitalID, String password) {
        this.hospitalID = hospitalID;
        this.password = password;
    }

    public abstract void menu();

    public boolean login(String id, String pass) {
        return hospitalID.equals(id) && password.equals(pass);
    }
}

