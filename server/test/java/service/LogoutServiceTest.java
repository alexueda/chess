package service;

import dataaccess.AuthDAO;
import model.AuthData;

public class LogoutServiceTest {

    public void testLogoutSuccess() {
        AuthDAO mockAuthDAO = new AuthDAO();
        String validAuthToken = "valid-token-123";
        mockAuthDAO.insertAuth(new AuthData(validAuthToken, "testuser"));
        LogoutService logoutService = new LogoutService(mockAuthDAO);
        logoutService.logout(validAuthToken);
        AuthData result = mockAuthDAO.getAuth(validAuthToken);
        assert result == null : "Auth token should be deleted after logout.";
    }

    public void testLogoutInvalidToken() {
        AuthDAO mockAuthDAO = new AuthDAO();
        LogoutService logoutService = new LogoutService(mockAuthDAO);
        boolean exceptionThrown = false;
        try {
            logoutService.logout("invalid-token-456");
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assert exceptionThrown : "Exception should be thrown for invalid auth token.";
    }

    public static void main(String[] args) {
        LogoutServiceTest test = new LogoutServiceTest();
        test.testLogoutSuccess();
        test.testLogoutInvalidToken();
        System.out.println("All tests passed.");
    }
}
