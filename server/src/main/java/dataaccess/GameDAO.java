package dataaccess;

import model.GameData;
import java.util.Map;

public interface GameDAO {
    GameData getGame(int gameID) throws DataAccessException;
    int insertGame(GameData game) throws DataAccessException;
    void clearGames() throws DataAccessException;
    Map<Integer, GameData> getAllGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
}
