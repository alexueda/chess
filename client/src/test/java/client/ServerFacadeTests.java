package client;

import org.junit.jupiter.api.*;
import service.ServerFacade;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        // Initialize the ServerFacade with the correct base URL for the server
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void testLoginSuccess() {
        String uniqueUsername = "testUser" + System.currentTimeMillis(); // Create a unique username
        String password = "password123";

        try {
            boolean registered = serverFacade.register(uniqueUsername, password, "test@example.com");
            Assertions.assertTrue(registered, "User registration failed for login test.");
            boolean loggedIn = serverFacade.login(uniqueUsername, password);
            Assertions.assertTrue(loggedIn, "Login should succeed for registered user.");
        } catch (Exception e) {
            Assertions.fail("Exception during login success test: " + e.getMessage());
        }
    }

    @Test
    public void testLoginFailure() {
        String username = "nonexistentUser";
        String password = "wrongPassword";
        try {
            boolean loginSuccess = serverFacade.login(username, password);
            assertFalse(loginSuccess, "Login should fail with incorrect credentials");
        } catch (Exception e) {
            fail("Exception during login failure test: " + e.getMessage());
        }
    }

    @Test
    public void testRegisterSuccess() {
        String uniqueUsername = "newUser" + System.currentTimeMillis(); // Generate a unique username
        String password = "password123";
        String email = "test@example.com";

        try {
            boolean registered = serverFacade.register(uniqueUsername, password, email);
            Assertions.assertTrue(registered, "Registration should succeed for a unique username.");
        } catch (Exception e) {
            Assertions.fail("Exception during successful registration test: " + e.getMessage());
        }
    }

    @Test
    public void testRegisterFailure() {
        String existingUsername = "existingUser";
        String password = "password123";
        String email = "existing@example.com";

        try {
            boolean initialRegister = serverFacade.register(existingUsername, password, email);
            Assertions.assertTrue(initialRegister, "Initial registration should succeed.");
            boolean duplicateRegister = serverFacade.register(existingUsername, password, email);
            Assertions.assertFalse(duplicateRegister, "Registration should fail for an existing username.");
        } catch (Exception e) {
            if (!e.getMessage().contains("Username already taken")) {
                Assertions.fail("Unexpected exception during registration failure test: " + e.getMessage());
            } else {
                System.out.println("Expected failure: " + e.getMessage());
            }
        }
    }

    @Test
    public void testLogoutSuccess() {
        String username = "testUser_" + System.currentTimeMillis(); // Use unique username
        String password = "password123";

        try {
            boolean registerResult = serverFacade.register(username, password, "test@example.com");
            Assertions.assertTrue(registerResult, "Registration should succeed.");

            boolean loginResult = serverFacade.login(username, password);
            Assertions.assertTrue(loginResult, "Login should succeed before logging out.");

            boolean logoutResult = serverFacade.logout();
            Assertions.assertTrue(logoutResult, "Logout should succeed after logging in.");

        } catch (Exception e) {
            Assertions.fail("Exception during logout success test: " + e.getMessage());
        }
    }

    @Test
    public void testLogoutFailureWithoutLogin() {
        try {
            boolean logoutResult = serverFacade.logout();
            Assertions.assertFalse(logoutResult, "Logout should fail when not logged in.");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Unauthorized") || e.getMessage().contains("401"),
                    "Expected Unauthorized or 401 error message when logging out without login. Actual message: " + e.getMessage());
        }
    }

}
