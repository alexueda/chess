package service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.UserData;
import model.AuthData;
import java.util.UUID;

public class RegisterService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService() {
        this.userDAO = new UserDAO();
        this.authDAO = new AuthDAO();
    }

    public AuthData register(UserData userData) {
        // Check if the user already exists
        if (userDAO.getUser(userData.username()) != null) {
            throw new IllegalArgumentException("Username already taken.");
        }

        // Insert the new user into the userDAO
        userDAO.insertUser(userData);

        // Generate auth token and store it in the authDAO
        AuthData authData = new AuthData(generateAuthToken(), userData.username());
        authDAO.insertAuth(authData);

        return authData;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
