package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
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
        activeSessions.put(session, new SessionInfo("", -1)); // Placeholder for session data
        System.out.println("WebSocket connection established: " + session.getRemoteAddress());
        sendNotification(session, "Connection established. Please send a CONNECT command with an authToken.");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received message: " + message); // Log incoming message
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
            System.err.println("Error processing WebSocket message: " + e.getMessage());
            sendErrorMessage(session, "Invalid WebSocket message format.");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        activeSessions.remove(session);
        System.out.println("WebSocket connection closed: " + session.getRemoteAddress() + ". Reason: " + reason);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
        if (session != null) activeSessions.remove(session);
    }

    private void handleConnect(Session session, UserGameCommand command) {
        try {
            // Validate authToken
            if (command.getAuthToken() == null || command.getAuthToken().isEmpty()) {
                sendErrorMessage(session, "AuthToken is required to connect.");
                return;
            }

            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                sendErrorMessage(session, "Invalid AuthToken.");
                return;
            }

            if (command.getGameID() == null) {
                sendErrorMessage(session, "GameID is required to connect.");
                return;
            }

            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendErrorMessage(session, "Invalid GameID.");
                return;
            }

            activeSessions.put(session, new SessionInfo(authData.authToken(), gameData.gameID()));
            System.out.println("User connected: " + authData.username() + " to game: " + gameData.gameName());

            //sendNotification(session, "Connected to game: " + gameData.gameName());
            broadcastNotification(authData.username() + " has joined the game.", gameData.gameID());

        } catch (Exception e) {
            System.err.println("Error in handleConnect: " + e.getMessage());
            sendErrorMessage(session, "An error occurred during connection.");
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

        boolean moveValid = true;

        if (moveValid) {
            System.out.println("Valid move made for game ID: " + command.getGameID());
            broadcastNotification("Player made a move.", command.getGameID());
        } else {
            sendErrorMessage(session, "Invalid move.");
        }
    }

    private void handleLeave(Session session) {
        SessionInfo sessionInfo = activeSessions.remove(session);

        if (!isAuthenticated(session)) {
            sendErrorMessage(session, "Unauthorized action. Please connect first.");
            return;
        }

        if (sessionInfo != null) {
            System.out.println("User left: " + sessionInfo.getAuthToken());
            broadcastNotification("User left: " + sessionInfo.getAuthToken(), sessionInfo.getGameID());
        }
    }

    private void handleResign(Session session) {
        SessionInfo sessionInfo = activeSessions.get(session);

        if (!isAuthenticated(session)) {
            sendErrorMessage(session, "Unauthorized action. Please connect first.");
            return;
        }

        if (sessionInfo != null) {
            System.out.println("User resigned: " + sessionInfo.getAuthToken());
            broadcastNotification("User resigned: " + sessionInfo.getAuthToken(), sessionInfo.getGameID());
        }
    }

    private void broadcastNotification(String message, int gameID) {
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        broadcast(gson.toJson(notification), gameID);
    }

    private void broadcast(String message, int gameID) {
        activeSessions.forEach((session, sessionInfo) -> {
            if (sessionInfo.getGameID() == gameID) {
                try {
                    if (session.isOpen()) {
                        session.getRemote().sendString(message);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to broadcast message: " + e.getMessage());
                }
            }
        });
    }

    private void sendErrorMessage(Session session, String errorMessage) {
        try {
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
            session.getRemote().sendString(gson.toJson(error));
        } catch (Exception e) {
            System.err.println("Failed to send error message: " + e.getMessage());
        }
    }

    private void sendNotification(Session session, String message) {
        try {
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            session.getRemote().sendString(gson.toJson(notification));
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }

    private boolean isAuthenticated(Session session) {
        SessionInfo sessionInfo = activeSessions.get(session);
        return sessionInfo != null && !sessionInfo.getAuthToken().isEmpty();
    }
}
