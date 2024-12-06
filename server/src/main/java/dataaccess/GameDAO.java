package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Map;

public interface GameDAO {
    GameData getGame(int gameID) throws DataAccessException;
    int insertGame(GameData gameData) throws DataAccessException;
    void updateGame(GameData gameData) throws DataAccessException;
    void updateGameState(int gameID, ChessGame updatedGame) throws DataAccessException; // Add this line
    Map<Integer, GameData> getAllGames() throws DataAccessException;
    void clearGames() throws DataAccessException;
}
