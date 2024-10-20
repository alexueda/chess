package service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;

public class ClearService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ClearService() {
        this.userDAO = new UserDAO();
        this.authDAO = new AuthDAO();
        this.gameDAO = new GameDAO();
    }

    public void clear() {
        userDAO.clearUsers();
        authDAO.clearAuth();
        gameDAO.clearGames();
    }
}
