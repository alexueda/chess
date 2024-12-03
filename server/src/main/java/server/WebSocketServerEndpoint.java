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
        activeSessions.put(session, null); // Store the session
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> handleConnect(command, session);
            case MAKE_MOVE -> handleMakeMove(command, session);
            case LEAVE -> handleLeave(session);
            case RESIGN -> handleResign(session);
        }
    }

    private void handleConnect(UserGameCommand command, Session session) {
        activeSessions.put(session, command.getAuthToken());
        broadcastNotification("User connected to game: " + command.getAuthToken());
    }

    private void handleMakeMove(UserGameCommand command, Session session) {
        // Process move logic and update the game state in the database
        broadcastNotification("Player made a move");
        broadcastGameUpdate(); // Notify all players with the new game state
    }

    private void handleLeave(Session session) {
        activeSessions.remove(session);
        broadcastNotification("A player has left the game");
    }

    private void handleResign(Session session) {
        broadcastNotification("A player has resigned from the game");
    }

    private void broadcastNotification(String message) {
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        broadcast(gson.toJson(notification));
    }

    private void broadcastGameUpdate() {
        // Generate a new game state message and broadcast
        ServerMessage gameUpdate = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, "Game updated");
        broadcast(gson.toJson(gameUpdate));
    }

    private void broadcast(String message) {
        activeSessions.keySet().forEach(session -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @OnClose
    public void onClose(Session session) {
        activeSessions.remove(session);
        System.out.println("Client disconnected: " + session.getId());
    }
}
