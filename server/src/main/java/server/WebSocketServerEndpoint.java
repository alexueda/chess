package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketServerEndpoint {

    private static final Map<Session, String> activeSessions = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onOpen(Session session) {
        //System.out.println("WebSocket connection established: " + session.getId());
        activeSessions.put(session, ""); // Initially no auth token
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMakeMove(session, command);
                case LEAVE -> handleLeave(session);
                case RESIGN -> handleResign(session);
                default -> sendErrorMessage(session, "Unknown command type: " + command.getCommandType());
            }
        } catch (Exception e) {
            System.err.println("Error processing WebSocket message: " + e.getMessage());
            sendErrorMessage(session, "Invalid WebSocket message.");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        activeSessions.remove(session);
        //System.out.println("WebSocket connection closed: " + session.getId() + ". Reason: " + reason.getReasonPhrase());
    }

//        @OnWebSocketError
//        public void onError(Session session, Throwable throwable) {
//            //System.err.println("WebSocket error for session " + session.getId() + ": " + throwable.getMessage());
//        }

    private void handleConnect(Session session, UserGameCommand command) {
        if (command.getAuthToken() == null) {
            sendErrorMessage(session, "AuthToken is required to connect.");
            return;
        }
        activeSessions.put(session, command.getAuthToken());
        System.out.println("User connected: " + command.getAuthToken());
        broadcastNotification("User connected to the game: " + command.getAuthToken());
    }

    private void handleMakeMove(Session session, UserGameCommand command) {
        if (!isAuthenticated(session)) {
            sendErrorMessage(session, "Unauthorized action. Please connect first.");
            return;
        }
        System.out.println("Move made for game ID: " + command.getGameID());
        broadcastNotification("Player made a move.");
    }

    private void handleLeave(Session session) {
        String userToken = activeSessions.remove(session);
        System.out.println("User left: " + userToken);
        broadcastNotification("User left: " + userToken);
    }

    private void handleResign(Session session) {
        String userToken = activeSessions.get(session);
        System.out.println("User resigned: " + userToken);
        broadcastNotification("User resigned: " + userToken);
    }

    private void broadcastNotification(String message) {
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        broadcast(gson.toJson(notification));
    }

    private void broadcast(String message) {
        activeSessions.keySet().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.getRemote().sendString(message);
                }
            } catch (Exception e) {
                System.err.println("Failed to broadcast message: " + e.getMessage());
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

    private boolean isAuthenticated(Session session) {
        return activeSessions.containsKey(session) && activeSessions.get(session) != null;
    }
}
