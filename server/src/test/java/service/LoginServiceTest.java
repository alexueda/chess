package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginServiceTest {

    private UserDAO mockUserDAO;
    private AuthDAO mockAuthDAO;
    private LoginService loginService;
    private ClearService clearService;

    @BeforeEach
    public void setup() throws DataAccessException {
        mockUserDAO = new SQLUserDAO();
        mockAuthDAO = new SQLAuthDAO();
        loginService = new LoginService(mockUserDAO, mockAuthDAO);
        clearService = new ClearService(mockUserDAO, mockAuthDAO, new SQLGameDAO());
        clearService.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Login Success")
    public void testLoginSuccess() throws DataAccessException {
        String username = "testuser";
        String password = "password123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());  // Hash password before storing
        mockUserDAO.insertUser(new UserData(username, hashedPassword, "testuser@mail.com"));
        AuthData authData = loginService.login(username, password);
        Assertions.assertNotNull(authData, "Login should be successful.");
        Assertions.assertNotNull(authData.authToken(), "Auth token should be generated.");
        Assertions.assertEquals(username, authData.username(), "Auth token should belong to the logged-in user.");
    }

    @Test
    @Order(2)
    @DisplayName("Login Failure - Incorrect Password")
    public void testLoginFailure() throws DataAccessException {
        String username = "testuser";
        String password = "password123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());  // Hash password before storing
        mockUserDAO.insertUser(new UserData(username, hashedPassword, "testuser@mail.com"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            loginService.login(username, "wrongpassword");
        }, "Exception should be thrown for invalid username or password.");
    }
}
