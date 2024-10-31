package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClearServiceTest {

    private UserDAO mockUserDAO;
    private AuthDAO mockAuthDAO;
    private GameDAO mockGameDAO;
    private ClearService clearService;

    @BeforeEach
    public void setup() throws DataAccessException {
        mockUserDAO = new SQLUserDAO();
        mockAuthDAO = new SQLAuthDAO();
        mockGameDAO = new SQLGameDAO();
        clearService = new ClearService(mockUserDAO, mockAuthDAO, mockGameDAO);
        clearService.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Clear Data Successfully")
    public void testClearSuccess() throws DataAccessException {
        UserData testUser = new UserData("testuser", "password", "test@mail.com");
        mockUserDAO.insertUser(testUser);
        mockAuthDAO.insertAuth(new AuthData("authToken123", testUser.username()));
        mockGameDAO.insertGame(new GameData(1, "testuser", "opponent", "testGame", null));
        clearService.clear();
        Assertions.assertNull(mockUserDAO.getUser(testUser.username()), "User data should be cleared.");
        Assertions.assertNull(mockAuthDAO.getAuth("authToken123"), "Auth data should be cleared.");
        Assertions.assertNull(mockGameDAO.getGame(1), "Game data should be cleared.");
    }

    @Test
    @Order(2)
    @DisplayName("Clear Data with No Existing Data")
    public void testClearNoData() throws DataAccessException {
        clearService.clear();
        Assertions.assertNull(mockUserDAO.getUser("nonExistentUser"), "No user data should exist.");
        Assertions.assertNull(mockAuthDAO.getAuth("nonExistentAuthToken"), "No auth data should exist.");
        Assertions.assertNull(mockGameDAO.getGame(999), "No game data should exist.");
    }
}
