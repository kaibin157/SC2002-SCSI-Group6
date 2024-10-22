public class StaffManagement {
    private String name;
    private String role;
    private String gender;
    private int age;

    public StaffManagement (String name, String role, String gender, int age) {
        this.name = name;
        this.role = role;
        this.gender = gender;
        this.age = age;

    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public String toString() {
        return "Staff [Name =" + name + ", Role =" + role + ", Gender =" + gender + ", Age =" + age + "]";
    }

}

