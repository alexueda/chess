package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.UserData;
import model.AuthData;
import java.util.UUID;

public class RegisterService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user) throws DataAccessException {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new IllegalArgumentException("Bad request: Missing required fields.");
        }
        if (userDAO.getUser(user.username()) != null) {
            throw new IllegalArgumentException("Username already taken.");
        }
        AuthData authData = new AuthData(generateAuthToken(), user.username());
        userDAO.insertUser(user);
        authDAO.insertAuth(authData);

        return authData;
    }
    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
