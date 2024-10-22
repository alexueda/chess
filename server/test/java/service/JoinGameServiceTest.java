package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

public class JoinGameServiceTest {

    public void testJoinGameSuccess() {
        AuthDAO mockAuthDAO = new AuthDAO();
        GameDAO mockGameDAO = new GameDAO();
        String validAuthToken = "authToken123";
        mockAuthDAO.insertAuth(new AuthData(validAuthToken, "testuser"));
        GameData game = new GameData(1, null, null, "testGame", null);
        mockGameDAO.insertGame(game);
        JoinGameService joinGameService = new JoinGameService(mockAuthDAO, mockGameDAO);
        try {
            joinGameService.joinGame(1, "WHITE", validAuthToken);
            GameData updatedGame = mockGameDAO.getGame(1);
            assert updatedGame != null : "Game should exist.";
            assert updatedGame.whiteUsername().equals("testuser") : "Player should be assigned to the white team.";
        } catch (Exception e) {
            assert false : "Exception should not be thrown in success case.";
        }
    }

    public void testJoinGameUnauthorized() {
        AuthDAO mockAuthDAO = new AuthDAO();
        GameDAO mockGameDAO = new GameDAO();
        GameData game = new GameData(1, null, null, "testGame", null);
        mockGameDAO.insertGame(game);
        JoinGameService joinGameService = new JoinGameService(mockAuthDAO, mockGameDAO);
        String invalidAuthToken = "invalidToken";
        boolean exceptionThrown = false;
        try {
            joinGameService.joinGame(1, "WHITE", invalidAuthToken);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        } catch (Exception e) {
            exceptionThrown = true;
        }

        assert exceptionThrown : "Exception should be thrown for unauthorized access.";
    }

    public static void main(String[] args) {
        JoinGameServiceTest test = new JoinGameServiceTest();
        test.testJoinGameSuccess();
        test.testJoinGameUnauthorized();
        System.out.println("All tests passed.");
    }
}
