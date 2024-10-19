package service;

import dataaccess.AuthDAO;
import model.AuthData;

public class LogoutService {
    private final AuthDAO authDAO;

    public LogoutService() {
        this.authDAO = new AuthDAO();
    }

    public void logout(String authToken) {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new IllegalArgumentException("Invalid auth token: Token does not exist.");
        }

        authDAO.deleteAuth(authToken);
    }
}
