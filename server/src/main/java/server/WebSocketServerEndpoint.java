package server;

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

            // Add client to active session
            activeSessions.put(session, new SessionInfo(authData.authToken(), gameData.gameID()));

            // Send LOAD_GAME to the root client
            sendLoadGame(session, gameData);

            // Send notification to other clients
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
        SessionInfo sessionInfo = activeSessions.get(session);

        if (!isAuthenticated(session)) {
            sendErrorMessage(session, "Unauthorized action. Please connect first.");
            return;
        }

        if (sessionInfo == null || sessionInfo.getGameID() != command.getGameID()) {
            sendErrorMessage(session, "Invalid GameID.");
            return;
        }

        boolean moveValid = validateMove(); // Placeholder for move validation logic.

        if (moveValid) {
            logInfo("Valid move made for game ID: " + command.getGameID(), session);
            broadcastLoadGame(command.getGameID());
            broadcastNotification("A player made a move.", command.getGameID(), session);
        } else {
            sendErrorMessage(session, "Invalid move.");
        }
    }

    private void handleResign(Session session) {
        if (!isAuthenticated(session)) {
            sendErrorMessage(session, "Unauthorized action. Please connect first.");
            return;
        }

        SessionInfo sessionInfo = activeSessions.get(session);
        if (sessionInfo != null) {
            logInfo("User resigned: " + sessionInfo.getAuthToken(), session);
            broadcastNotification("User resigned: " + sessionInfo.getAuthToken(), sessionInfo.getGameID(), session);
        }
    }

    private void handleLeave(Session session) {
        if (!isAuthenticated(session)) {
            sendErrorMessage(session, "Unauthorized action. Please connect first.");
            return;
        }

        SessionInfo sessionInfo = activeSessions.remove(session);
        if (sessionInfo != null) {
            logInfo("User left: " + sessionInfo.getAuthToken(), session);
            broadcastNotification("User left: " + sessionInfo.getAuthToken(), sessionInfo.getGameID(), session);
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
        // Placeholder for actual move validation logic.
        return true;
    }


    private void sendErrorMessage(Session session, String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "Error: An unexpected error occurred.";
        }

        // Use ErrorMessage class to construct the error response
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