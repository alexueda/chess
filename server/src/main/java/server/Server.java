package server;

import spark.*;
import handler.*;
import dataaccess.*;

public class Server {

    public int run(int desiredPort) {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database initialization failed: " + e.getMessage());
        }

        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        UserDAO userDAO = new SQLUserDAO();  // Use SQLUserDAO implementation
        AuthDAO authDAO = new SQLAuthDAO();  // Use SQLAuthDAO implementation
        GameDAO gameDAO = new SQLGameDAO();  // Use SQLGameDAO implementation

        // Register your endpoints and handle exceptions here.
        // Initialize all handler instances
        ClearHandler clearHandler = new ClearHandler(userDAO, authDAO, gameDAO);
        RegisterHandler registerHandler = new RegisterHandler(userDAO, authDAO);
        LoginHandler loginHandler = new LoginHandler(userDAO, authDAO);
        LogoutHandler logoutHandler = new LogoutHandler(authDAO);
        ListGamesHandler listGamesHandler = new ListGamesHandler(authDAO, gameDAO);
        CreateGameHandler createGameHandler = new CreateGameHandler(authDAO, gameDAO);
        JoinGameHandler joinGameHandler = new JoinGameHandler(authDAO, gameDAO);

        // Define your endpoints and map them to the corresponding handler methods
        Spark.delete("/db", (req, res) -> clearHandler.handleClear(req, res)); // Clear database
        Spark.post("/user", (req, res) -> registerHandler.handleRegister(req, res)); // Register user
        Spark.post("/session", (req, res) -> loginHandler.handleLogin(req, res)); // Login
        Spark.delete("/session", (req, res) -> logoutHandler.handleLogout(req, res)); // Logout
        Spark.get("/game", (req, res) -> listGamesHandler.handleListGames(req, res)); // List games
        Spark.post("/game", (req, res) -> createGameHandler.handleCreateGame(req, res)); // Create game
        Spark.put("/game", (req, res) -> joinGameHandler.handleJoinGame(req, res)); // Join game

        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
