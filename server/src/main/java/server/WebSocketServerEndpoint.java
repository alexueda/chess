package server;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws")
public class WebSocketServerEndpoint {

    private static final Map<Session, String> activeSessions = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Client connected: " + session.getId());
        activeSessions.put(session, null); // Store session without authentication token for now
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(command, session);
                case MAKE_MOVE -> handleMakeMove(command, session);
                case LEAVE -> handleLeave(session);
                case RESIGN -> handleResign(session);
                default -> System.err.println("Unknown command type: " + command.getCommandType());
            }
        } catch (Exception e) {
            System.err.println("Error processing WebSocket message: " + e.getMessage());
            sendErrorMessage(session, "Invalid WebSocket message format.");
        }
    }

    private void handleConnect(UserGameCommand command, Session session) {
        if (command.getAuthToken() == null) {
            sendErrorMessage(session, "AuthToken is required to connect.");
            return;
        }

        activeSessions.put(session, command.getAuthToken());
        System.out.println("User connected: " + command.getAuthToken());
        broadcastNotification("User connected to game: " + command.getAuthToken());
    }

    private void handleMakeMove(UserGameCommand command, Session session) {
        if (!isAuthenticated(session)) {
            sendErrorMessage(session, "Unauthorized action. Please connect first.");
            return;
        }

        System.out.println("Processing move for game ID: " + command.getGameID());
        broadcastNotification("Player made a move.");
    }

    private void handleLeave(Session session) {
        String userToken = activeSessions.remove(session);
        System.out.println("User left: " + userToken);
        broadcastNotification("User has left the game: " + userToken);
    }

    private void handleResign(Session session) {
        String userToken = activeSessions.get(session);
        System.out.println("User resigned: " + userToken);
        broadcastNotification("User has resigned from the game: " + userToken);
    }

    private void broadcastNotification(String message) {
        ServerMessage notification = new ServerMessage();
        notification.setServerMessageType(ServerMessage.ServerMessageType.NOTIFICATION);
        notification.setMessage(message);
        broadcast(gson.toJson(notification));
    }

    private void broadcast(String message) {
        activeSessions.keySet().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                System.err.println("Failed to send WebSocket message: " + e.getMessage());
            }
        });
    }

    private void sendErrorMessage(Session session, String errorMessage) {
        try {
            ServerMessage error = new ServerMessage();
            error.setServerMessageType(ServerMessage.ServerMessageType.ERROR);
            error.setMessage(errorMessage);
            session.getBasicRemote().sendText(gson.toJson(error));
        } catch (Exception e) {
            System.err.println("Failed to send error message: " + e.getMessage());
        }
    }

    private boolean isAuthenticated(Session session) {
        return activeSessions.containsKey(session) && activeSessions.get(session) != null;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        activeSessions.remove(session);
        System.out.println("Client disconnected: " + session.getId() + ". Reason: " + reason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error for session " + (session != null ? session.getId() : "unknown") + ": " + throwable.getMessage());
    }
}
