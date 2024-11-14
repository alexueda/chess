package client;

import org.junit.jupiter.api.*;
import service.ServerFacade;
import server.Server;
import model.GameData;

import java.util.List;

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

    @BeforeEach
    public void cleanup() {
        try {
            // Attempt to logout if still logged in after each test
            serverFacade.logout();
        } catch (Exception ignored) {
            // Ignore any exceptions, as some tests may leave us in a non-logged-in state
        }
    }

    @Test
    public void testLoginSuccess() {
        String uniqueUsername = "testUser" + System.currentTimeMillis();
        String password = "password123";

        try {
            boolean registered = serverFacade.register(uniqueUsername, password, "test@example.com");
            assertTrue(registered, "User registration failed for login test.");

            boolean loggedIn = serverFacade.login(uniqueUsername, password);
            assertTrue(loggedIn, "Login should succeed for registered user.");

            serverFacade.logout();
        } catch (Exception e) {
            fail("Exception during login success test: " + e.getMessage());
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
        String uniqueUsername = "newUser" + System.currentTimeMillis();
        String password = "password123";
        String email = "test@example.com";

        try {
            boolean registered = serverFacade.register(uniqueUsername, password, email);
            assertTrue(registered, "Registration should succeed for a unique username.");

            serverFacade.logout();
        } catch (Exception e) {
            fail("Exception during successful registration test: " + e.getMessage());
        }
    }

    @Test
    public void testRegisterFailure() {
        String existingUsername = "existingUser" + System.currentTimeMillis();
        String password = "password123";
        String email = "existing@example.com";

        try {
            boolean initialRegister = serverFacade.register(existingUsername, password, email);
            assertTrue(initialRegister, "Initial registration should succeed.");

            boolean duplicateRegister = serverFacade.register(existingUsername, password, email);
            assertFalse(duplicateRegister, "Registration should fail for an existing username.");

            serverFacade.logout();
        } catch (Exception e) {
            if (!e.getMessage().contains("Username already taken")) {
                fail("Unexpected exception during registration failure test: " + e.getMessage());
            }
        }
    }

    @Test
    public void testLogoutSuccess() {
        String username = "testUser_" + System.currentTimeMillis();
        String password = "password123";

        try {
            boolean registerResult = serverFacade.register(username, password, "test@example.com");
            assertTrue(registerResult, "Registration should succeed.");

            boolean loginResult = serverFacade.login(username, password);
            assertTrue(loginResult, "Login should succeed before logging out.");

            boolean logoutResult = serverFacade.logout();
            assertTrue(logoutResult, "Logout should succeed after logging in.");
        } catch (Exception e) {
            fail("Exception during logout success test: " + e.getMessage());
        }
    }

    @Test
    public void testLogoutFailureWithoutLogin() {
        try {
            boolean logoutResult = serverFacade.logout();
            assertFalse(logoutResult, "Logout should fail when not logged in.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Unauthorized") || e.getMessage().contains("401"),
                    "Expected Unauthorized or 401 error message when logging out without login. Actual message: " + e.getMessage());
        }
    }

    @Test
    public void testCreateGameSuccess() {
        String username = "gameCreator_" + System.currentTimeMillis();
        String password = "password123";
        String gameName = "testGame_" + System.currentTimeMillis();

        try {
            serverFacade.register(username, password, "test@example.com");
            serverFacade.login(username, password);

            boolean gameCreated = serverFacade.createGame(gameName);
            assertTrue(gameCreated, "Game creation should succeed.");

            serverFacade.logout();
        } catch (Exception e) {
            fail("Exception during game creation success test: " + e.getMessage());
        }
    }

    @Test
    public void testCreateGameFailureWithoutLogin() {
        String gameName = "unauthenticatedGame_" + System.currentTimeMillis();

        try {
            boolean gameCreated = serverFacade.createGame(gameName);
            assertFalse(gameCreated, "Game creation should fail without login.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Unauthorized") || e.getMessage().contains("401"),
                    "Expected Unauthorized or 401 error message when creating game without login. Actual message: " + e.getMessage());
        }
    }

    @Test
    public void testListGamesSuccess() {
        String username = "gameLister_" + System.currentTimeMillis();
        String password = "password123";

        try {
            serverFacade.register(username, password, "test@example.com");
            serverFacade.login(username, password);

            List<GameData> games = serverFacade.listGames();
            assertNotNull(games, "Game list should not be null.");
            assertTrue(games.size() >= 0, "Game list should be empty or contain games.");

            serverFacade.logout();
        } catch (Exception e) {
            fail("Exception during game list success test: " + e.getMessage());
        }
    }

    @Test
    public void testListGamesFailureWithoutLogin() {
        try {
            List<GameData> games = serverFacade.listGames();
            fail("Game listing should fail without login.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Unauthorized") || e.getMessage().contains("401"),
                    "Expected Unauthorized or 401 error message when listing games without login. Actual message: " + e.getMessage());
        }
    }

    @Test
    public void testJoinGameSuccess() {
        String username = "joinGameUser_" + System.currentTimeMillis();
        String password = "password123";
        String gameName = "joinTestGame_" + System.currentTimeMillis();

        try {
            // Register and log in the user
            serverFacade.register(username, password, "test@example.com");
            serverFacade.login(username, password);

            // Create a new game
            boolean gameCreated = serverFacade.createGame(gameName);
            assertTrue(gameCreated, "Game creation should succeed.");

            // Retrieve the game list to get the newly created game's ID
            List<GameData> games = serverFacade.listGames();
            Integer gameId = null;
            for (GameData game : games) {
                if (game.gameName().equals(gameName)) {
                    gameId = game.gameID();  // Use the correct method to get the game ID
                    break;
                }
            }
            assertNotNull(gameId, "Game ID should be valid for the newly created game.");

            // Attempt to join the game as "WHITE" or "BLACK" based on availability
            boolean joinedGame = false;
            try {
                joinedGame = serverFacade.joinGame(gameId, "WHITE");
            } catch (Exception e) {
                if (e.getMessage().contains("already taken")) {
                    // If "WHITE" is taken, try "BLACK"
                    joinedGame = serverFacade.joinGame(gameId, "BLACK");
                } else {
                    throw e;
                }
            }

            assertTrue(joinedGame, "Joining game should succeed with an available color.");

            // Logout after the test
            serverFacade.logout();
        } catch (Exception e) {
            fail("Exception during join game success test: " + e.getMessage());
        }
    }



    @Test
    public void testJoinGameFailureWithInvalidID() {
        String username = "joinGameFailUser_" + System.currentTimeMillis();
        String password = "password123";

        try {
            serverFacade.register(username, password, "test@example.com");
            serverFacade.login(username, password);

            int invalidGameId = -1;
            boolean joinedGame = serverFacade.joinGame(invalidGameId, "WHITE");
            assertFalse(joinedGame, "Joining game should fail with invalid game ID.");

            serverFacade.logout();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Invalid game ID") || e.getMessage().contains("not found"),
                    "Expected error message when joining game with invalid ID. Actual message: " + e.getMessage());
        }
    }
}
