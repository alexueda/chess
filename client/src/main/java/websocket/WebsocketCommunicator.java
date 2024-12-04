package websocket;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessageObserver;

import javax.websocket.*;
import java.net.URI;

public class WebsocketCommunicator {

    private final ServerMessageObserver observer;
    private Session session;
    private final Gson gson = new Gson();

    public WebsocketCommunicator(String serverDomain, ServerMessageObserver observer) throws Exception {
        this.observer = observer;
        connectToServer(serverDomain);
    }

    private void connectToServer(String serverDomain) throws Exception {
        try {
            URI uri = new URI("ws://" + serverDomain + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    System.out.println("WebSocket connection established.");
                }

//                @Override
//                public void onError(Session session, Throwable throwable) {
//                    System.err.println("WebSocket error: " + throwable.getMessage());
//                }
            }, uri);

            session.addMessageHandler((MessageHandler.Whole<String>) this::handleMessage);
        } catch (Exception e) {
            System.err.println("Failed to connect to WebSocket server: " + e.getMessage());
            throw e;
        }
    }

    private void handleMessage(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
            observer.notify(serverMessage);
        } catch (Exception e) {
            System.err.println("Error processing WebSocket message: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        if (session == null || !session.isOpen()) {
            System.err.println("Cannot send message. WebSocket is not open.");
            return;
        }
        session.getAsyncRemote().sendText(message);
    }

    public void close() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
                System.out.println("WebSocket closed.");
            }
        } catch (Exception e) {
            System.err.println("Error closing WebSocket: " + e.getMessage());
        }
    }
}
