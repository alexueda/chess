package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData newUser) throws DataAccessException {
        if (newUser.username() == null || newUser.username().isEmpty() ||
                newUser.password() == null || newUser.password().isEmpty() ||
                newUser.email() == null || newUser.email().isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }

        String hashedPassword = BCrypt.hashpw(newUser.password(), BCrypt.gensalt());
        UserData hashedUser = new UserData(newUser.username(), hashedPassword, newUser.email());

        try {
            userDAO.insertUser(hashedUser);
            String authToken = java.util.UUID.randomUUID().toString();
            AuthData authData = new AuthData(authToken, newUser.username());
            authDAO.insertAuth(authData);
            return authData;
        } catch (DataAccessException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new IllegalArgumentException("Username already taken", e);
            }
            throw e;
        }
    }
}