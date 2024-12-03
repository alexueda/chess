package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private String message;

    public ErrorMessage() {
        super(ServerMessageType.ERROR);
    }

    public String getMessage() {
        return message;
    }
}
