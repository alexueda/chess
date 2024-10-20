package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

public class JoinGameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public JoinGameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void joinGame(int gameID, String playerColor, String authToken) throws Exception {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new Exception("Unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new Exception("Game not found");
        }

        GameData updatedGame; //This is variable

        if (playerColor.equals("WHITE") && game.whiteUsername() == null) {
            updatedGame = new GameData(game.gameID(), authData.username(), game.blackUsername(), game.gameName(), game.game());
        }
        else if (playerColor.equals("BLACK") && game.blackUsername() == null) {
            updatedGame = new GameData(game.gameID(), game.whiteUsername(), authData.username(), game.gameName(), game.game());
        }
        else {
            throw new Exception("Color already taken: This color is already assigned to another player.");
        }

        gameDAO.updateGame(updatedGame);
    }
}
