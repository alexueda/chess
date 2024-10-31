package service;

import dataaccess.*;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegisterServiceTest {

    private UserDAO mockUserDAO;
    private AuthDAO mockAuthDAO;
    private RegisterService registerService;
    private ClearService clearService;

    @BeforeEach
    public void setup() throws DataAccessException {
        mockUserDAO = new SQLUserDAO();
        mockAuthDAO = new SQLAuthDAO();
        registerService = new RegisterService(mockUserDAO, mockAuthDAO);
        clearService = new ClearService(mockUserDAO, mockAuthDAO, new SQLGameDAO());
        clearService.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Register Success")
    public void testRegisterSuccess() throws DataAccessException {
        UserData newUser = new UserData("testuser", "password123", "testuser@mail.com");
        AuthData result = registerService.register(newUser);
        Assertions.assertNotNull(mockUserDAO.getUser(newUser.username()), "User should be successfully registered.");
        Assertions.assertNotNull(mockAuthDAO.getAuth(result.authToken()), "Auth token should be generated.");
    }

    @Test
    @Order(2)
    @DisplayName("Register Failure - Missing Fields")
    public void testRegisterMissingFields() {
        UserData incompleteUser = new UserData("testuser", null, "testuser@mail.com");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            registerService.register(incompleteUser);
        }, "Exception should be thrown for missing required fields.");
    }

    @Test
    @Order(3)
    @DisplayName("Register Failure - Duplicate Username")
    public void testRegisterDuplicateUsername() throws DataAccessException {
        UserData newUser = new UserData("duplicateUser", "password123", "duplicate@mail.com");
        registerService.register(newUser);  // Register once
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            registerService.register(newUser);
        }, "Exception should be thrown for duplicate username.");
    }
}
