package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

public class LoginServiceTest {

    public void testLoginSuccess() {
        UserDAO mockUserDAO = new UserDAO();
        AuthDAO mockAuthDAO = new AuthDAO();
        String username = "testuser";
        String password = "password123";
        mockUserDAO.insertUser(new UserData(username, password, "testuser@mail.com"));
        LoginService loginService = new LoginService(mockUserDAO, mockAuthDAO);
        AuthData authData = loginService.login(username, password);
        assert authData != null : "Login should be successful.";
        assert authData.authToken() != null : "Auth token should be generated.";
        assert authData.username().equals(username) : "Auth token should belong to the logged-in user.";
    }

    public void testLoginFailure() {
        UserDAO mockUserDAO = new UserDAO();
        AuthDAO mockAuthDAO = new AuthDAO();
        String username = "testuser";
        String password = "password123";
        mockUserDAO.insertUser(new UserData(username, password, "testuser@mail.com"));
        LoginService loginService = new LoginService(mockUserDAO, mockAuthDAO);
        boolean exceptionThrown = false;
        try {
            loginService.login(username, "wrongpassword");
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assert exceptionThrown : "Exception should be thrown for invalid username or password.";
    }

    public static void main(String[] args) {
        LoginServiceTest test = new LoginServiceTest();
        test.testLoginSuccess();
        test.testLoginFailure();
        System.out.println("All tests passed.");
    }
}
