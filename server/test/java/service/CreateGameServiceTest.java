package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

public class CreateGameServiceTest {

    public void testCreateGameSuccess() {
        AuthDAO mockAuthDAO = new AuthDAO();
        GameDAO mockGameDAO = new GameDAO();
        String validAuthToken = "authToken123";
        mockAuthDAO.insertAuth(new AuthData(validAuthToken, "testuser"));
        CreateGameService createGameService = new CreateGameService(mockAuthDAO, mockGameDAO);
        try {
            GameData gameData = createGameService.createGame("testGame", validAuthToken);

            assert gameData != null : "Game should be created successfully.";
            assert gameData.gameName().equals("testGame") : "Game name should match.";
            assert gameData.gameID() > 0 : "Game ID should be valid.";
        } catch (Exception e) {
            assert false : "Exception should not be thrown in success case.";
        }
    }

    public void testCreateGameUnauthorized() {
        AuthDAO mockAuthDAO = new AuthDAO();
        GameDAO mockGameDAO = new GameDAO();
        CreateGameService createGameService = new CreateGameService(mockAuthDAO, mockGameDAO);
        String invalidAuthToken = "invalidToken";
        boolean exceptionThrown = false;
        try {
            createGameService.createGame("testGame", invalidAuthToken);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assert exceptionThrown : "Exception should be thrown for unauthorized access.";
    }

    public static void main(String[] args) {
        CreateGameServiceTest test = new CreateGameServiceTest();
        test.testCreateGameSuccess();
        test.testCreateGameUnauthorized();
        System.out.println("All tests passed.");
    }
}
