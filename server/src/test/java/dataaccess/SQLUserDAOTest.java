package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest {

    private SQLUserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new SQLUserDAO();
        userDAO.clearUsers(); // Clear the table before each test to ensure isolation
    }

    @Test
    @DisplayName("Positive Test: Insert User Successfully")
    public void testInsertUserSuccess() throws DataAccessException {
        UserData user = new UserData("testuser", "hashedPassword123", "testuser@mail.com");
        userDAO.insertUser(user);

        UserData retrievedUser = userDAO.getUser("testuser");
        assertNotNull(retrievedUser, "User should be retrieved after insertion");
        assertEquals("testuser", retrievedUser.username());
        assertEquals("hashedPassword123", retrievedUser.password());
        assertEquals("testuser@mail.com", retrievedUser.email());
    }

    @Test
    @DisplayName("Negative Test: Insert Duplicate User")
    public void testInsertDuplicateUser() throws DataAccessException {
        UserData user = new UserData("duplicateUser", "hashedPassword123", "duplicateUser@mail.com");
        userDAO.insertUser(user);

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userDAO.insertUser(user);
        }, "Inserting a duplicate user should throw DataAccessException");

        assertTrue(exception.getMessage().contains("Duplicate entry"), "Exception should indicate a duplicate entry");
    }

    @Test
    @DisplayName("Positive Test: Get Existing User")
    public void testGetUserSuccess() throws DataAccessException {
        UserData user = new UserData("existingUser", "hashedPassword123", "existingUser@mail.com");
        userDAO.insertUser(user);

        UserData retrievedUser = userDAO.getUser("existingUser");
        assertNotNull(retrievedUser, "User should be retrieved successfully");
        assertEquals("existingUser", retrievedUser.username());
        assertEquals("hashedPassword123", retrievedUser.password());
        assertEquals("existingUser@mail.com", retrievedUser.email());
    }

    @Test
    @DisplayName("Negative Test: Get Non-Existent User")
    public void testGetUserNotFound() throws DataAccessException {
        UserData user = userDAO.getUser("nonExistentUser");
        assertNull(user, "Retrieving a non-existent user should return null");
    }

    @Test
    @DisplayName("Positive Test: Clear Users Table")
    public void testClearUsers() throws DataAccessException {
        UserData user1 = new UserData("user1", "hashedPassword1", "user1@mail.com");
        UserData user2 = new UserData("user2", "hashedPassword2", "user2@mail.com");
        userDAO.insertUser(user1);
        userDAO.insertUser(user2);

        userDAO.clearUsers();
        assertNull(userDAO.getUser("user1"), "User1 should not be retrievable after clear");
        assertNull(userDAO.getUser("user2"), "User2 should not be retrievable after clear");
    }
}
