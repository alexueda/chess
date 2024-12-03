package websocket.messages;

public class NotificationMessage extends ServerMessage {
    private String message;

    public NotificationMessage() {
        super(ServerMessageType.NOTIFICATION);
    }

    public String getMessage() {
        return message;
    }
}
