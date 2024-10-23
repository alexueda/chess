package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateGameServiceTest {

    private AuthDAO mockAuthDAO;
    private GameDAO mockGameDAO;
    private CreateGameService createGameService;

    @BeforeEach
    public void setup() {
        mockAuthDAO = new AuthDAO();
        mockGameDAO = new GameDAO();
        createGameService = new CreateGameService(mockAuthDAO, mockGameDAO);
    }

    @Test
    @Order(1)
    @DisplayName("Create Game Successfully")
    public void testCreateGameSuccess() {
        // Arrange
        String validAuthToken = "authToken123";
        mockAuthDAO.insertAuth(new AuthData(validAuthToken, "testuser"));

        // Act & Assert
        try {
            GameData gameData = createGameService.createGame("testGame", validAuthToken);

            Assertions.assertNotNull(gameData, "Game should be created successfully.");
            Assertions.assertEquals("testGame", gameData.gameName(), "Game name should match.");
            Assertions.assertTrue(gameData.gameID() > 0, "Game ID should be valid.");
        } catch (Exception e) {
            Assertions.fail("Exception should not be thrown in success case.");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Create Game Unauthorized")
    public void testCreateGameUnauthorized() {
        // Arrange
        String invalidAuthToken = "invalidToken";

        // Act & Assert
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            createGameService.createGame("testGame", invalidAuthToken);
        }, "Exception should be thrown for unauthorized access.");

        Assertions.assertNotNull(exception, "Exception should be thrown for invalid auth token.");
    }
}
