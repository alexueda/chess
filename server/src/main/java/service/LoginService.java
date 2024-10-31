package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class LoginService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData login(String username, String password) throws DataAccessException {
        UserData user = userDAO.getUser(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (!BCrypt.checkpw(password, user.password())) {
            throw new IllegalArgumentException("Incorrect password");
        }
        String authToken = generateAuthToken();
        AuthData authData = new AuthData(authToken, username);
        authDAO.insertAuth(authData);
        return authData;
    }

    private String generateAuthToken() {
        return java.util.UUID.randomUUID().toString();
    }
}
