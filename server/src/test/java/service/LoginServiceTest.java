package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginServiceTest {

    private UserDAO mockUserDAO;
    private AuthDAO mockAuthDAO;
    private LoginService loginService;

    @BeforeEach
    public void setup() {
        mockUserDAO = new SQLUserDAO();
        mockAuthDAO = new SQLAuthDAO();
        loginService = new LoginService(mockUserDAO, mockAuthDAO);
    }

    @Test
    @Order(1)
    @DisplayName("Login Success")
    public void testLoginSuccess() throws DataAccessException {
        // Arrange
        String username = "testuser";
        String password = "password123";
        mockUserDAO.insertUser(new UserData(username, password, "testuser@mail.com"));

        // Act
        AuthData authData = loginService.login(username, password);

        // Assert
        Assertions.assertNotNull(authData, "Login should be successful.");
        Assertions.assertNotNull(authData.authToken(), "Auth token should be generated.");
        Assertions.assertEquals(username, authData.username(), "Auth token should belong to the logged-in user.");
    }

    @Test
    @Order(2)
    @DisplayName("Login Failure - Incorrect Password")
    public void testLoginFailure() throws DataAccessException {
        // Arrange
        String username = "testuser";
        String password = "password123";
        mockUserDAO.insertUser(new UserData(username, password, "testuser@mail.com"));

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            loginService.login(username, "wrongpassword");
        }, "Exception should be thrown for invalid username or password.");
    }
}
