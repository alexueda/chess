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
        Spark.webSocket("/ws", WebSocketServerEndpoint.class);

        UserDAO userDAO = new SQLUserDAO();
        AuthDAO authDAO = new SQLAuthDAO();
        GameDAO gameDAO = new SQLGameDAO();

        ClearHandler clearHandler = new ClearHandler(userDAO, authDAO, gameDAO);
        RegisterHandler registerHandler = new RegisterHandler(userDAO, authDAO);
        LoginHandler loginHandler = new LoginHandler(userDAO, authDAO);
        LogoutHandler logoutHandler = new LogoutHandler(authDAO);
        ListGamesHandler listGamesHandler = new ListGamesHandler(authDAO, gameDAO);
        CreateGameHandler createGameHandler = new CreateGameHandler(authDAO, gameDAO);
        JoinGameHandler joinGameHandler = new JoinGameHandler(authDAO, gameDAO);

        Spark.delete("/db", (req, res) -> clearHandler.handleClear(req, res));
        Spark.post("/user", (req, res) -> registerHandler.handleRegister(req, res));
        Spark.post("/session", (req, res) -> loginHandler.handleLogin(req, res));
        Spark.delete("/session", (req, res) -> logoutHandler.handleLogout(req, res));
        Spark.get("/game", (req, res) -> listGamesHandler.handleListGames(req, res));
        Spark.post("/game", (req, res) -> createGameHandler.handleCreateGame(req, res));
        Spark.put("/game", (req, res) -> joinGameHandler.handleJoinGame(req, res));

        Spark.init();
        Spark.awaitInitialization();
        System.out.println("Server running on port " + desiredPort);
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
        System.out.println("Server stopped.");
    }
}
