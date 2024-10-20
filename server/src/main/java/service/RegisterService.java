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
        // Check if the username is already taken
        if (userDAO.getUser(userData.username()) != null) {
            throw new IllegalArgumentException("Username already taken.");
        }

        userDAO.insertUser(userData);

        // Generate an auth token for the new user
        AuthData authData = new AuthData(generateAuthToken(), userData.username());

        authDAO.insertAuth(authData);

        return authData;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
