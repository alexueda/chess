package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTest {

    private SQLAuthDAO authDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        authDAO.clearAuths(); // Clear the table before each test for isolation
    }

    @Test
    @DisplayName("Positive Test: Insert Auth Successfully")
    public void testInsertAuthSuccess() throws DataAccessException {
        AuthData authData = new AuthData("token123", "testuser");
        authDAO.insertAuth(authData);
        AuthData retrievedAuth = authDAO.getAuth("token123");
        assertNotNull(retrievedAuth, "Auth data should be retrievable after insertion");
        assertEquals("testuser", retrievedAuth.username(), "Retrieved username should match the inserted username");
    }

    @Test
    @DisplayName("Negative Test: Insert Duplicate Auth Token")
    public void testInsertDuplicateAuthToken() throws DataAccessException {
        AuthData authData = new AuthData("token123", "testuser");
        authDAO.insertAuth(authData);
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            authDAO.insertAuth(authData);
        }, "Inserting duplicate auth token should throw DataAccessException");
        assertTrue(exception.getMessage().contains("Duplicate entry"), "Exception should indicate duplicate entry");
    }

    @Test
    @DisplayName("Positive Test: Get Existing Auth")
    public void testGetAuthSuccess() throws DataAccessException {
        AuthData authData = new AuthData("token456", "anotheruser");
        authDAO.insertAuth(authData);
        AuthData retrievedAuth = authDAO.getAuth("token456");
        assertNotNull(retrievedAuth, "Auth data should be retrieved successfully");
        assertEquals("anotheruser", retrievedAuth.username(), "Username should match the one used in insertion");
    }

    @Test
    @DisplayName("Negative Test: Get Non-Existent Auth")
    public void testGetAuthNotFound() throws DataAccessException {
        AuthData authData = authDAO.getAuth("nonexistenttoken");
        assertNull(authData, "Retrieving non-existent auth data should return null");
    }

    @Test
    @DisplayName("Positive Test: Delete Auth Successfully")
    public void testDeleteAuthSuccess() throws DataAccessException {
        AuthData authData = new AuthData("token789", "deleteuser");
        authDAO.insertAuth(authData);
        authDAO.deleteAuth("token789");
        AuthData retrievedAuth = authDAO.getAuth("token789");
        assertNull(retrievedAuth, "Auth data should not be retrievable after deletion");
    }

    @Test
    @DisplayName("Negative Test: Delete Non-Existent Auth")
    public void testDeleteAuthNotFound() throws DataAccessException {
        assertDoesNotThrow(() -> authDAO.deleteAuth("nonexistenttoken"), "Deleting non-existent auth should not throw an exception");
    }

    @Test
    @DisplayName("Positive Test: Clear Auths Table")
    public void testClearAuths() throws DataAccessException {
        AuthData auth1 = new AuthData("token101", "user1");
        AuthData auth2 = new AuthData("token102", "user2");
        authDAO.insertAuth(auth1);
        authDAO.insertAuth(auth2);
        authDAO.clearAuths();
        assertNull(authDAO.getAuth("token101"), "Auth1 should not be retrievable after clear");
        assertNull(authDAO.getAuth("token102"), "Auth2 should not be retrievable after clear");
    }
}
