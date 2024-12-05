package websocket.messages;

public class ServerMessage {
    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    private ServerMessageType serverMessageType;
    private String message; // Optional field for notifications or errors
    private Object game;    // Optional field for game state in LOAD_GAME

    // Default constructor
    public ServerMessage() {}

    // Constructor for messages with only ServerMessageType
    public ServerMessage(ServerMessageType serverMessageType) {
        this.serverMessageType = serverMessageType;
    }

    // Constructor for NOTIFICATION and ERROR messages
    public ServerMessage(ServerMessageType serverMessageType, String message) {
        this.serverMessageType = serverMessageType;
        this.message = message;
    }

    // Constructor for LOAD_GAME messages
    public ServerMessage(ServerMessageType serverMessageType, Object game) {
        this.serverMessageType = serverMessageType;
        this.game = game;
    }

    // Getters
    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public String getMessage() {
        return message;
    }

    public Object getGame() {
        return game;
    }

    // Setters
    public void setServerMessageType(ServerMessageType serverMessageType) {
        this.serverMessageType = serverMessageType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setGame(Object game) {
        this.game = game;
    }
}
