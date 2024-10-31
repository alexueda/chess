package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ListGamesServiceTest {

    private AuthDAO mockAuthDAO;
    private GameDAO mockGameDAO;
    private ListGamesService listGamesService;
    private ClearService clearService;

    @BeforeEach
    public void setup() throws DataAccessException {
        mockAuthDAO = new SQLAuthDAO();
        mockGameDAO = new SQLGameDAO();
        listGamesService = new ListGamesService(mockAuthDAO, mockGameDAO);
        clearService = new ClearService(new SQLUserDAO(), mockAuthDAO, mockGameDAO);
        clearService.clear();
    }

    @Test
    @Order(1)
    @DisplayName("List All Games Successfully")
    public void testListAllGamesSuccess() throws DataAccessException {
        String validAuthToken = "authToken123";
        mockAuthDAO.insertAuth(new AuthData(validAuthToken, "testuser"));
        mockGameDAO.insertGame(new GameData(1, "testuser1", null, "testGame1", null));
        mockGameDAO.insertGame(new GameData(2, "testuser2", null, "testGame2", null));
        List<GameData> games = listGamesService.listAllGames(validAuthToken);
        Assertions.assertEquals(2, games.size(), "There should be 2 games listed.");
        Assertions.assertEquals("testGame1", games.get(0).gameName(), "First game should be 'testGame1'.");
        Assertions.assertEquals("testGame2", games.get(1).gameName(), "Second game should be 'testGame2'.");
    }

    @Test
    @Order(2)
    @DisplayName("List All Games Unauthorized")
    public void testListAllGamesUnauthorized() throws DataAccessException {
        mockGameDAO.insertGame(new GameData(1, "testuser1", null, "testGame1", null));
        String invalidAuthToken = "invalidToken";
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            listGamesService.listAllGames(invalidAuthToken);
        }, "Exception should be thrown for unauthorized access.");
        Assertions.assertNotNull(exception, "Exception should be thrown for invalid auth token.");
    }
}
