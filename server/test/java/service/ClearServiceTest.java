package service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.UserData;
import model.AuthData;
import model.GameData;

public class ClearServiceTest {
    // Positive test
    public void testClearSuccess() {
        UserDAO mockUserDAO = new UserDAO();
        AuthDAO mockAuthDAO = new AuthDAO();
        GameDAO mockGameDAO = new GameDAO();
        UserData testUser = new UserData("testuser", "password", "test@mail.com");
        mockUserDAO.insertUser(testUser);
        mockAuthDAO.insertAuth(new AuthData("authToken123", testUser.username()));
        mockGameDAO.insertGame(new GameData(1, "testuser", "opponent", "testGame", null));
        ClearService clearService = new ClearService(mockUserDAO, mockAuthDAO, mockGameDAO);
        clearService.clear();
        assert mockUserDAO.getUser(testUser.username()) == null : "User data should be cleared.";
        assert mockAuthDAO.getAuth("authToken123") == null : "Auth data should be cleared.";
        assert mockGameDAO.getGame(1) == null : "Game data should be cleared.";
    }

    // Negative test
    public void testClearNoData() {
        UserDAO mockUserDAO = new UserDAO();
        AuthDAO mockAuthDAO = new AuthDAO();
        GameDAO mockGameDAO = new GameDAO();
        ClearService clearService = new ClearService(mockUserDAO, mockAuthDAO, mockGameDAO);
        clearService.clear();
        assert mockUserDAO.getUser("nonExistentUser") == null : "No user data should exist.";
        assert mockAuthDAO.getAuth("nonExistentAuthToken") == null : "No auth data should exist.";
        assert mockGameDAO.getGame(999) == null : "No game data should exist.";
    }

    public static void main(String[] args) {
        ClearServiceTest test = new ClearServiceTest();
        test.testClearSuccess();
        test.testClearNoData();
        System.out.println("All tests passed.");
    }
}
