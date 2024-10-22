package server;

import spark.*;
import handler.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        // Initialize all handler instances
        ClearHandler clearHandler = new ClearHandler();
        RegisterHandler registerHandler = new RegisterHandler();
        LoginHandler loginHandler = new LoginHandler();
        LogoutHandler logoutHandler = new LogoutHandler();
        ListGamesHandler listGamesHandler = new ListGamesHandler();
        CreateGameHandler createGameHandler = new CreateGameHandler();
        JoinGameHandler joinGameHandler = new JoinGameHandler();

        // Define your endpoints and map them to the corresponding handler methods
        Spark.delete("/db", (req, res) -> clearHandler.handleClear(req, res)); // Clear database
        Spark.post("/user", (req, res) -> registerHandler.handleRegister(req, res)); // Register user
        Spark.post("/session", (req, res) -> loginHandler.handleLogin(req, res)); // Login
        Spark.delete("/session", (req, res) -> logoutHandler.handleLogout(req, res)); // Logout
        Spark.get("/game", (req, res) -> listGamesHandler.handleListGames(req, res)); // List games
        Spark.post("/game", (req, res) -> createGameHandler.handleCreateGame(req, res)); // Create game
        Spark.put("/game", (req, res) -> joinGameHandler.handleJoinGame(req, res)); // Join game


        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
