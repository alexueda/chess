package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketServerEndpoint {

    private static final Map<Session, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    private final AuthDAO authDAO = new SQLAuthDAO();
    private final GameDAO gameDAO = new SQLGameDAO();

    private static class SessionInfo {
        private final String authToken;
        private final int gameID;

        public SessionInfo(String authToken, int gameID) {
            this.authToken = authToken;
            this.gameID = gameID;
        }

        public String getAuthToken() {
            return authToken;
        }

        public int getGameID() {
            return gameID;
        }
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
        activeSessions.put(session, new SessionInfo("", -1));
        logInfo("WebSocket connection established", session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        logInfo("Received message: " + message, session);
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            if (command == null || command.getCommandType() == null) {
                sendErrorMessage(session, "Invalid or missing commandType in WebSocket message.");
                return;
            }

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMakeMove(session, command);
                case RESIGN -> handleResign(session);
                case LEAVE -> handleLeave(session);
                default -> sendErrorMessage(session, "Unknown command type: " + command.getCommandType());
            }
        } catch (Exception e) {
            logError("Error processing WebSocket message", e, session);
            sendErrorMessage(session, "Invalid WebSocket message format.");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        activeSessions.remove(session);
        logInfo("WebSocket connection closed. Reason: " + reason, session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        logError("WebSocket error occurred", throwable, session);
        if (session != null) activeSessions.remove(session);
    }

    private void handleConnect(Session session, UserGameCommand command) {
        try {
            validateConnectCommand(command); // Validate command for null fields

            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                sendErrorMessage(session, "Error: Invalid AuthToken provided.");
                return;
            }

            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendErrorMessage(session, "Error: Invalid GameID provided.");
                return;
            }

            activeSessions.put(session, new SessionInfo(authData.authToken(), gameData.gameID()));

            sendLoadGame(session, gameData);

            broadcastNotification(
                    authData.username() + " joined as " + (authData.username().equals(gameData.whiteUsername()) ? "white" : "black"),
                    gameData.gameID(),
                    session
            );

            logInfo("User connected: " + authData.username() + " to game: " + gameData.gameName(), session);

        } catch (IllegalArgumentException e) {
            logError(e.getMessage(), e, session);
            sendErrorMessage(session, "Error: " + e.getMessage());
        } catch (Exception e) {
            logError("Unexpected error during connection", e, session);
            sendErrorMessage(session, "Error: An unexpected error occurred.");
        }
    }


    private void handleMakeMove(Session session, UserGameCommand command) {
        try {
            SessionInfo sessionInfo = activeSessions.get(session);
            if (sessionInfo == null || !isAuthenticated(session)) {
                sendErrorMessage(session, "Error: Unauthorized action. Please connect first.");
                return;
            }

            if (sessionInfo.getGameID() != command.getGameID()) {
                sendErrorMessage(session, "Error: Invalid GameID.");
                return;
            }

            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                sendErrorMessage(session, "Error: Invalid auth token.");
                return;
            }

            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendErrorMessage(session, "Error: Game not found.");
                return;
            }

            ChessGame game = gameData.game();
            if (game == null) {
                game = new ChessGame(); // Initialize a new ChessGame if null
                logInfo("Game state was null; initialized a new ChessGame.", session);
            }

            ChessMove move = gson.fromJson(gson.toJson(command.getMove()), ChessMove.class);

            String currentPlayer = game.getTeamTurn() == ChessGame.TeamColor.WHITE
                    ? gameData.whiteUsername()
                    : gameData.blackUsername();
            if (!authData.username().equals(currentPlayer)) {
                sendErrorMessage(session, "Error: It is not your turn.");
                return;
            }

            try {
                game.makeMove(move);
            } catch (InvalidMoveException e) {
                sendErrorMessage(session, "Error: " + e.getMessage());
                return;
            }

            gameDAO.updateGame(new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            ));

            broadcastLoadGame(command.getGameID());

            broadcastNotification("A move was made: " + move.toString(), gameData.gameID(), session);

            if (game.isInCheckmate(game.getTeamTurn())) {
                broadcastNotification("Game over: Checkmate! " + game.getTeamTurn() + " loses.", gameData.gameID(), null);
            } else if (game.isInStalemate(game.getTeamTurn())) {
                broadcastNotification("Game over: Stalemate! It's a draw.", gameData.gameID(), null);
            }

        } catch (Exception e) {
            logError("Error handling MAKE_MOVE command", e, session);
            sendErrorMessage(session, "Error: Unable to process the move.");
        }
    }



    private void handleResign(Session session) {
        try {
            if (!isAuthenticated(session)) {
                sendErrorMessage(session, "Unauthorized action. Please connect first.");
                return;
            }

            // Retrieve session information
            SessionInfo sessionInfo = activeSessions.get(session);
            if (sessionInfo == null) {
                sendErrorMessage(session, "Session is not associated with any game.");
                return;
            }

            GameData gameData = gameDAO.getGame(sessionInfo.getGameID());
            if (gameData == null) {
                sendErrorMessage(session, "Game not found.");
                return;
            }

            ChessGame game = gameData.game();
            if (game == null) {
                game = new ChessGame();
                logInfo("Game state was null; initialized a new ChessGame.", session);
            }

            if (game.isGameOver()) {
                sendErrorMessage(session, "Game is already over. Cannot resign.");
                return;
            }

            AuthData resigningPlayerAuth = authDAO.getAuth(sessionInfo.getAuthToken());
            if (resigningPlayerAuth == null) {
                sendErrorMessage(session, "Invalid authentication token.");
                return;
            }

            String resigningUsername = resigningPlayerAuth.username();

            if (!resigningUsername.equals(gameData.whiteUsername()) && !resigningUsername.equals(gameData.blackUsername())) {
                sendErrorMessage(session, "Observers cannot resign.");
                return;
            }

            String losingPlayer = resigningUsername;
            String winningPlayer = (resigningUsername.equals(gameData.whiteUsername()))
                    ? gameData.blackUsername()
                    : gameData.whiteUsername();

            game.setGameOver(true);

            String resignationMessage = losingPlayer + " has resigned. " + winningPlayer + " wins!";
            broadcastNotification(resignationMessage, gameData.gameID(), null);

            gameDAO.updateGame(new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            ));

            logInfo("Player resigned: " + resigningUsername, session);

        } catch (Exception e) {
            logError("Error handling RESIGN command", e, session);
            sendErrorMessage(session, "An error occurred while processing the resignation.");
        }
    }




    private void handleLeave(Session session) {
        try {
            if (!isAuthenticated(session)) {
                sendErrorMessage(session, "Unauthorized action. Please connect first.");
                return;
            }

            SessionInfo sessionInfo = activeSessions.remove(session);
            if (sessionInfo == null) {
                sendErrorMessage(session, "Session is not associated with any game.");
                return;
            }

            GameData gameData = gameDAO.getGame(sessionInfo.getGameID());
            if (gameData == null) {
                sendErrorMessage(session, "Game not found.");
                return;
            }

            AuthData leavingPlayerAuth = authDAO.getAuth(sessionInfo.getAuthToken());
            String leavingUsername = leavingPlayerAuth != null ? leavingPlayerAuth.username() : null;

            boolean isPlayer = false;

            // Check if the leaving user is a player (white or black)
            if (leavingUsername != null && leavingUsername.equals(gameData.whiteUsername())) {
                gameData = new GameData(
                        gameData.gameID(),
                        null,  // Remove white player
                        gameData.blackUsername(),
                        gameData.gameName(),
                        gameData.game()
                );
                isPlayer = true;
            } else if (leavingUsername != null && leavingUsername.equals(gameData.blackUsername())) {
                gameData = new GameData(
                        gameData.gameID(),
                        gameData.whiteUsername(),
                        null,  // Remove black player
                        gameData.gameName(),
                        gameData.game()
                );
                isPlayer = true;
            }

            // Update the game state if a player has left
            if (isPlayer) {
                gameDAO.updateGame(gameData);
            }

            // Prepare notification message
            String notificationMessage;
            if (isPlayer) {
                notificationMessage = leavingUsername + " has left the game.";
            } else {
                notificationMessage = "An observer has left the game.";
            }

            // Broadcast notification to remaining sessions
            broadcastNotification(notificationMessage, gameData.gameID(), session);

            if (isPlayer) {
                logInfo("User left: " + leavingUsername, session);
            } else {
                logInfo("Observer left: " + sessionInfo.getAuthToken(), session);
            }

            // Close the session
            session.close();

        } catch (Exception e) {
            logError("Error handling LEAVE command", e, session);
            sendErrorMessage(session, "An error occurred while processing leave.");
        }
    }


    private boolean isAuthenticated(Session session) {
        SessionInfo sessionInfo = activeSessions.get(session);
        return sessionInfo != null && sessionInfo.getAuthToken() != null && !sessionInfo.getAuthToken().isEmpty();
    }


    private void sendLoadGame(Session session, GameData gameData) {
        ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
        sendMessage(session, gson.toJson(loadGameMessage));
    }

    private void broadcastNotification(String message, int gameID, Session excludeSession) {
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        String notificationJson = gson.toJson(notification);

        activeSessions.forEach((session, sessionInfo) -> {
            if (sessionInfo.getGameID() == gameID && !session.equals(excludeSession)) {
                sendMessage(session, notificationJson);
            }
        });
    }

    private void broadcastLoadGame(int gameID) {
        activeSessions.forEach((session, sessionInfo) -> {
            if (sessionInfo.getGameID() == gameID) {
                try {
                    GameData gameData = gameDAO.getGame(gameID);
                    sendLoadGame(session, gameData);
                } catch (Exception e) {
                    logError("Error broadcasting load game", e, session);
                }
            }
        });
    }

    private boolean validateMove() {
        return true;
    }


    private void sendErrorMessage(Session session, String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "Error: An unexpected error occurred.";
        }

        ErrorMessage error = new ErrorMessage(errorMessage);

        sendMessage(session, gson.toJson(error));
    }


    private void sendMessage(Session session, String message) {
        try {
            if (session.isOpen()) {
                session.getRemote().sendString(message);
            }
        } catch (Exception e) {
            logError("Failed to send message", e, session);
        }
    }

    private void validateConnectCommand(UserGameCommand command) {
        if (command.getAuthToken() == null || command.getAuthToken().isEmpty()) {
            throw new IllegalArgumentException("AuthToken is required to connect.");
        }
        if (command.getGameID() == null) {
            throw new IllegalArgumentException("GameID is required to connect.");
        }
    }

    private void logInfo(String message, Session session) {
        System.out.println("[INFO] " + message + " - Session: " + session.getRemoteAddress());
    }

    private void logError(String message, Throwable throwable, Session session) {
        System.err.println("[ERROR] " + message + " - Session: " + session.getRemoteAddress());
        throwable.printStackTrace();
    }
}
