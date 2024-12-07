package service;

import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginServiceTest extends AbstractServiceTest {

    private LoginService loginService;

    @BeforeEach
    @Override
    public void setup() throws DataAccessException {
        super.setup();
        loginService = new LoginService(mockUserDAO, mockAuthDAO);
    }

    @Test
    @Order(1)
    @DisplayName("Login Success")
    public void testLoginSuccess() throws DataAccessException {
        String username = "testuser";
        String password = "password123";
        insertUser(username, password, "testuser@mail.com");

        AuthData authData = loginService.login(username, password);
        verifyAuthData(authData, username);
    }

    @Test
    @Order(2)
    @DisplayName("Login Failure - Incorrect Password")
    public void testLoginFailure() throws DataAccessException {
        String username = "testuser";
        String password = "password123";
        insertUser(username, password, "testuser@mail.com");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            loginService.login(username, "wrongpassword");
        }, "Exception should be thrown for invalid username or password.");
    }
}
