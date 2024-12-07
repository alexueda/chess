package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractServiceTest {

    protected UserDAO mockUserDAO;
    protected AuthDAO mockAuthDAO;
    protected ClearService clearService;

    @BeforeEach
    public void setup() throws DataAccessException {
        mockUserDAO = new SQLUserDAO();
        mockAuthDAO = new SQLAuthDAO();
        clearService = new ClearService(mockUserDAO, mockAuthDAO, new SQLGameDAO());
        clearService.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Setup Success")
    public void testSetupSuccess() {
        Assertions.assertNotNull(mockUserDAO, "UserDAO should be initialized.");
        Assertions.assertNotNull(mockAuthDAO, "AuthDAO should be initialized.");
        Assertions.assertNotNull(clearService, "ClearService should be initialized.");
    }

    protected void insertUser(String username, String password, String email) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        mockUserDAO.insertUser(new UserData(username, hashedPassword, email));
    }

    protected void verifyAuthData(AuthData authData, String username) {
        Assertions.assertNotNull(authData, "AuthData should not be null.");
        Assertions.assertNotNull(authData.authToken(), "Auth token should not be null.");
        Assertions.assertEquals(username, authData.username(), "Auth token should belong to the expected user.");
    }
}
