package websocket;

import com.google.gson.Gson;
import spark.Session;
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

        try {
            URI uri = new URI("ws://" + serverDomain + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    System.out.println("Connected to WebSocket server.");
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    System.out.println("WebSocket connection closed: " + closeReason.getReasonPhrase());
                }

                @Override
                public void onError(Session session, Throwable thr) {
                    System.err.println("WebSocket error: " + thr.getMessage());
                }
            }, uri);

            session.addMessageHandler((MessageHandler.Whole<String>) this::handleMessage);
        } catch (Exception e) {
            throw new Exception("Failed to connect to WebSocket server.", e);
        }
    }

    private void handleMessage(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
        observer.notify(serverMessage);
    }

    public void sendMessage(String message) {
        session.getAsyncRemote().sendText(message);
    }

    public void close() throws Exception {
        if (session != null) {
            session.close();
        }
    }
}
