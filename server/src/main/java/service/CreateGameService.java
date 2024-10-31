package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

public class CreateGameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public CreateGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public int createGame(String gameName, String authToken) throws Exception {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new Exception("Unauthorized");
        }
        GameData newGame = new GameData(0, null, null, gameName, null);
        int gameID = gameDAO.insertGame(newGame);
        return gameID;
    }
}
