package websocket.messages;

public interface ServerMessageObserver {
    void notify(ServerMessage message);
}
