package websocket.messages;

public class ServerMessage {
    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    private ServerMessageType serverMessageType;
    private String message;  // For notifications and errors
    private Object game;     // For game updates

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public String getMessage() {
        return message;
    }

    public Object getGame() {
        return game;
    }
}
