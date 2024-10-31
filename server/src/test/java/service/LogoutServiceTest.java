package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogoutServiceTest {

    private AuthDAO mockAuthDAO;
    private LogoutService logoutService;

    @BeforeEach
    public void setup() {
        mockAuthDAO = new SQLAuthDAO();
        logoutService = new LogoutService(mockAuthDAO);
    }

    @Test
    @Order(1)
    @DisplayName("Logout Success")
    public void testLogoutSuccess() throws DataAccessException {
        // Arrange
        String validAuthToken = "valid-token-123";
        mockAuthDAO.insertAuth(new AuthData(validAuthToken, "testuser"));

        // Act
        logoutService.logout(validAuthToken);

        // Assert
        AuthData result = mockAuthDAO.getAuth(validAuthToken);
        Assertions.assertNull(result, "Auth token should be deleted after logout.");
    }

    @Test
    @Order(2)
    @DisplayName("Logout Invalid Token")
    public void testLogoutInvalidToken() {
        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            logoutService.logout("invalid-token-456");
        }, "Exception should be thrown for invalid auth token.");
    }
}
