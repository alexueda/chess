package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JoinGameServiceTest {

    private AuthDAO mockAuthDAO;
    private GameDAO mockGameDAO;
    private JoinGameService joinGameService;

    @BeforeEach
    public void setup() {
        mockAuthDAO = new SQLAuthDAO();
        mockGameDAO = new SQLGameDAO();
        joinGameService = new JoinGameService(mockAuthDAO, mockGameDAO);
    }

    @Test
    @Order(1)
    @DisplayName("Join Game Successfully")
    public void testJoinGameSuccess() throws DataAccessException {
        // Arrange
        String validAuthToken = "authToken123";
        mockAuthDAO.insertAuth(new AuthData(validAuthToken, "testuser"));
        GameData game = new GameData(1, null, null, "testGame", null);
        mockGameDAO.insertGame(game);

        // Act & Assert
        try {
            joinGameService.joinGame(1, "WHITE", validAuthToken);
            GameData updatedGame = mockGameDAO.getGame(1);

            Assertions.assertNotNull(updatedGame, "Game should exist.");
            Assertions.assertEquals("testuser", updatedGame.whiteUsername(), "Player should be assigned to the white team.");
        } catch (Exception e) {
            Assertions.fail("Exception should not be thrown in success case.");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Join Game Unauthorized")
    public void testJoinGameUnauthorized() throws DataAccessException {
        // Arrange
        GameData game = new GameData(1, null, null, "testGame", null);
        mockGameDAO.insertGame(game);
        String invalidAuthToken = "invalidToken";

        // Act & Assert
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            joinGameService.joinGame(1, "WHITE", invalidAuthToken);
        }, "Exception should be thrown for unauthorized access.");

        Assertions.assertNotNull(exception, "Exception should be thrown for invalid auth token.");
    }
}
