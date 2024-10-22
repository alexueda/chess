package service;

import dataaccess.*;
import model.UserData;
import model.AuthData;
import java.util.UUID;

public class LoginService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData login(String username, String password) {
        UserData user = userDAO.getUser(username);
        if (user == null || !user.password().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        AuthData authData = new AuthData(generateAuthToken(), username);
        authDAO.insertAuth(authData);
        return authData;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }



}

