package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import java.util.ArrayList;
import java.util.List;

public class ListGamesService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ListGamesService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public List<GameData> listAllGames(String authToken) {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        // Convert Map values to List because if not this code will throw error
        return new ArrayList<>(gameDAO.getAllGames().values());
    }
}
