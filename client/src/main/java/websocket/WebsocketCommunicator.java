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

        try {
            URI uri = new URI("ws://" + serverDomain + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    System.out.println("WebSocket connection established.");
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

            if (this.session == null) {
                throw new IllegalStateException("Failed to open WebSocket session.");
            }

            this.session.addMessageHandler((MessageHandler.Whole<String>) this::handleMessage);
        } catch (Exception e) {
            System.err.println("Failed to connect to WebSocket server: " + e.getMessage());
            throw new Exception("Failed to connect to WebSocket server.", e);
        }
    }

    private void handleMessage(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
            observer.notify(serverMessage);
        } catch (Exception e) {
            System.err.println("Failed to process WebSocket message: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        if (session == null || !session.isOpen()) {
            System.err.println("WebSocket session is not open. Cannot send message.");
            return;
        }

        try {
            session.getAsyncRemote().sendText(message);
        } catch (Exception e) {
            System.err.println("Failed to send WebSocket message: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            System.err.println("Failed to close WebSocket session: " + e.getMessage());
        }
    }
}
