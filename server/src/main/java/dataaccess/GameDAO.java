package dataaccess;

import model.GameData;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {

    private Map<Integer, GameData> gameTable = new HashMap<>();

    public void insertGame(GameData game) {
        gameTable.put(game.gameID(), game);
    }

    public GameData getGame(int gameID) {
        return gameTable.get(gameID);
    }

    public Map<Integer, GameData> getAllGames() {
        return gameTable;
    }

    public void clearGames() {
        gameTable.clear();
    }

    public void updateGame(GameData game) {
        gameTable.put(game.gameID(), game);
    }

}