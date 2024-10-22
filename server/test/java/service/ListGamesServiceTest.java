package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

import java.util.List;

public class ListGamesServiceTest {

    public void testListAllGamesSuccess() {
        AuthDAO mockAuthDAO = new AuthDAO();
        GameDAO mockGameDAO = new GameDAO();
        String validAuthToken = "authToken123";
        mockAuthDAO.insertAuth(new AuthData(validAuthToken, "testuser"));
        mockGameDAO.insertGame(new GameData(1, "testuser1", null, "testGame1", null));
        mockGameDAO.insertGame(new GameData(2, "testuser2", null, "testGame2", null));
        ListGamesService listGamesService = new ListGamesService(mockAuthDAO, mockGameDAO);
        List<GameData> games = listGamesService.listAllGames(validAuthToken);
        assert games.size() == 2 : "There should be 2 games listed.";
        assert games.get(0).gameName().equals("testGame1") : "First game should be 'testGame1'.";
        assert games.get(1).gameName().equals("testGame2") : "Second game should be 'testGame2'.";
    }

    public void testListAllGamesUnauthorized() {
        AuthDAO mockAuthDAO = new AuthDAO();
        GameDAO mockGameDAO = new GameDAO();
        mockGameDAO.insertGame(new GameData(1, "testuser1", null, "testGame1", null));
        ListGamesService listGamesService = new ListGamesService(mockAuthDAO, mockGameDAO);
        String invalidAuthToken = "invalidToken";
        boolean exceptionThrown = false;
        try {
            listGamesService.listAllGames(invalidAuthToken);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assert exceptionThrown : "Exception should be thrown for unauthorized access.";
    }

    public static void main(String[] args) {
        ListGamesServiceTest test = new ListGamesServiceTest();
        test.testListAllGamesSuccess();
        test.testListAllGamesUnauthorized();
        System.out.println("All tests passed.");
    }
}
