package service;

import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegisterServiceTest extends AbstractServiceTest {

    private RegisterService registerService;

    @BeforeEach
    @Override
    public void setup() throws DataAccessException {
        super.setup();
        registerService = new RegisterService(mockUserDAO, mockAuthDAO);
    }

    @Test
    @Order(1)
    @DisplayName("Register Success")
    public void testRegisterSuccess() throws DataAccessException {
        UserData newUser = new UserData("testuser", "password123", "testuser@mail.com");
        AuthData authData = registerService.register(newUser);

        Assertions.assertNotNull(mockUserDAO.getUser(newUser.username()), "User should be successfully registered.");
        Assertions.assertNotNull(mockAuthDAO.getAuth(authData.authToken()), "Auth token should be generated.");
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
        registerService.register(newUser); // Register once

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            registerService.register(newUser);
        }, "Exception should be thrown for duplicate username.");
    }
}
