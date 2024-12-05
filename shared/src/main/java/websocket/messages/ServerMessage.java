package websocket.messages;

public class ServerMessage {
    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    private ServerMessageType serverMessageType;
    private String message;
    private Object game;

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

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public String getMessage() {
        return message;
    }

    public Object getGame() {
        return game;
    }

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