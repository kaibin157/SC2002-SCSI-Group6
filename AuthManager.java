package oop;

import java.io.IOException;

public interface AuthManager {
    boolean authenticate(String hospitalID, String password) throws IOException;
    void updatePassword(String hospitalID, String newPassword) throws IOException;
}
