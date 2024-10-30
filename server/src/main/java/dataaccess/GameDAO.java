package dataaccess;

import model.GameData;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {

    private Map<Integer, GameData> gameTable = new HashMap<>();
    private int nextGameID = 1;

    public void insertGame(GameData game) {
        gameTable.put(game.gameID(), game);
    }

    public int generateGameID() {
        return nextGameID++;
    }

    public GameData getGame(int gameID) {
        return gameTable.get(gameID);
    }

    public Map<Integer, GameData> getAllGames() {
        return gameTable;
    }

    public void clearGames() {
        gameTable.clear();
        nextGameID = 1;
    }

    public void updateGame(GameData game) {
        gameTable.put(game.gameID(), game);
    }
}