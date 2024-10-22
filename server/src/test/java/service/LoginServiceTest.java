package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.AuthData;
import model.UserData;

public class LoginServiceTest {

    private LoginService loginService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        loginService = new LoginService(userDAO, authDAO);

        UserData existingUser = new UserData("existingUser", "password123", "test@example.com");
        userDAO.insertUser(existingUser);
    }

    @Test
    public void testSuccessfulLogin() {
        AuthData authData = loginService.login("existingUser", "password123");

        assertNotNull(authData.authToken());
        assertEquals("existingUser", authData.username());
    }

    @Test
    public void testLoginFailsForInvalidPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            loginService.login("existingUser", "wrongPassword");
        });
    }
}
