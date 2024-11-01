package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTest {

    private SQLGameDAO gameDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        gameDAO = new SQLGameDAO();
        gameDAO.clearGames(); // Clear the table before each test to ensure isolation
    }

    @Test
    @DisplayName("Positive Test: Insert Game Successfully")
    public void testInsertGameSuccess() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(1, "player1", "player2", "TestGame", chessGame);
        int generatedID = gameDAO.insertGame(gameData);
        assertTrue(generatedID > 0, "Game should be inserted with a valid generated ID");
        GameData retrievedGame = gameDAO.getGame(1);
        assertNotNull(retrievedGame, "Game should be retrieved after insertion");
        assertEquals("player1", retrievedGame.whiteUsername());
        assertEquals("player2", retrievedGame.blackUsername());
        assertEquals("TestGame", retrievedGame.gameName());
        assertEquals(gameData.gameID(), retrievedGame.gameID());
    }

    @Test
    @DisplayName("Negative Test: Insert Duplicate Game ID")
    public void testInsertDuplicateGameID() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(1, "player1", "player2", "TestGame", chessGame);
        gameDAO.insertGame(gameData);
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameDAO.insertGame(gameData);
        }, "Inserting a game with a duplicate ID should throw DataAccessException");
        assertTrue(exception.getMessage().contains("Duplicate entry"), "Exception should indicate a duplicate entry");
    }

    @Test
    @DisplayName("Positive Test: Get Existing Game")
    public void testGetGameSuccess() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(2, "player1", "player2", "TestGame", chessGame);
        gameDAO.insertGame(gameData);
        GameData retrievedGame = gameDAO.getGame(2);
        assertNotNull(retrievedGame, "Game should be retrieved successfully");
        assertEquals("player1", retrievedGame.whiteUsername());
        assertEquals("player2", retrievedGame.blackUsername());
        assertEquals("TestGame", retrievedGame.gameName());
        assertEquals(gameData.gameID(), retrievedGame.gameID());
    }

    @Test
    @DisplayName("Negative Test: Get Non-Existent Game")
    public void testGetGameNotFound() throws DataAccessException {
        GameData game = gameDAO.getGame(99);
        assertNull(game, "Retrieving a non-existent game should return null");
    }

    @Test
    @DisplayName("Positive Test: Update Game Successfully")
    public void testUpdateGameSuccess() throws DataAccessException {
        ChessGame initialGame = new ChessGame();
        GameData gameData = new GameData(3, "player1", "player2", "InitialGame", initialGame);
        gameDAO.insertGame(gameData);
        ChessGame updatedGame = new ChessGame();
        GameData updatedData = new GameData(3, "player1", "player2", "UpdatedGame", updatedGame);
        gameDAO.updateGame(updatedData);
        GameData retrievedGame = gameDAO.getGame(3);
        assertNotNull(retrievedGame, "Game should be retrievable after update");
        assertEquals("UpdatedGame", retrievedGame.gameName(), "Game name should be updated");
    }

    @Test
    @DisplayName("Negative Test: Update Non-Existent Game")
    public void testUpdateGameNotFound() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(100, "player1", "player2", "NonExistentGame", game);
        assertDoesNotThrow(() -> {
            gameDAO.updateGame(gameData);
        }, "Updating a non-existent game should not throw an exception");
        GameData retrievedGame = gameDAO.getGame(100);
        assertNull(retrievedGame, "No game should be found for a non-existent game ID");
    }

    @Test
    @DisplayName("Positive Test: Get All Games")
    public void testGetAllGames() throws DataAccessException {
        ChessGame game1 = new ChessGame();
        ChessGame game2 = new ChessGame();
        gameDAO.insertGame(new GameData(1, "player1", "player2", "Game1", game1));
        gameDAO.insertGame(new GameData(2, "player3", "player4", "Game2", game2));
        Map<Integer, GameData> games = gameDAO.getAllGames();
        assertEquals(2, games.size(), "There should be two games retrieved");
        assertNotNull(games.get(1), "Game1 should be retrievable");
        assertNotNull(games.get(2), "Game2 should be retrievable");
    }

    @Test
    @DisplayName("Positive Test: Clear Games Table")
    public void testClearGames() throws DataAccessException {
        ChessGame game1 = new ChessGame();
        ChessGame game2 = new ChessGame();
        gameDAO.insertGame(new GameData(1, "player1", "player2", "Game1", game1));
        gameDAO.insertGame(new GameData(2, "player3", "player4", "Game2", game2));
        gameDAO.clearGames();
        assertNull(gameDAO.getGame(1), "Game1 should not be retrievable after clear");
        assertNull(gameDAO.getGame(2), "Game2 should not be retrievable after clear");
    }
}
