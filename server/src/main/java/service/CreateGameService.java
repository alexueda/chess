package service;

import dataaccess.GameDAO;
import model.GameData;

public class CreateGameService {

    private final GameDAO gameDAO;

    public CreateGameService() {
        this.gameDAO = new GameDAO();
    }

    public GameData createGame(String gameName) {
        int newGameID = gameDAO.generateGameID();
        GameData newGame = new GameData(newGameID, null, null, gameName, null);
        gameDAO.insertGame(newGame);

        return newGame;
    }
}
