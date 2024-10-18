package service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.UserData;
import model.AuthData;

public class RegisterService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService() {
        this.userDAO = new UserDAO();
        this.authDAO = new AuthDAO();
    }

    public AuthData register(String username, String password, String email) {
        if (userDAO.getUser(username) != null) {
            throw new IllegalArgumentException("Username already taken.");
        }
        UserData newUser = new UserData(username, password, email);
        userDAO.insertUser(newUser);

        AuthData authData = new AuthData(generateAuthToken(), username);
        authDAO.insertAuth(authData);

        return authData;
    }

    private String generateAuthToken() {
        return "authToken" + Math.random(); // Placeholder for right now. How should I create token???
    }
}
