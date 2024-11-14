package service;

import com.google.gson.Gson;
import java.util.Map;

public class ServerFacade {
    private final ClientCommunicator communicator;
    private final Gson gson = new Gson();

    public ServerFacade() {
        this.communicator = new ClientCommunicator();
    }

    public boolean login(String username, String password) throws Exception {
        Map<String, String> credentials = Map.of("username", username, "password", password);
        String response = communicator.sendPostRequest("/session", gson.toJson(credentials));

        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("authToken")) {
            communicator.setAuthToken((String) responseMap.get("authToken"));
            return true;
        } else {
            throw new Exception("Login failed: " + responseMap.getOrDefault("message", "Unknown error"));
        }
    }

    public boolean register(String username, String password, String email) throws Exception {
        Map<String, String> credentials = Map.of("username", username, "password", password, "email", email);
        String response = communicator.sendPostRequest("/user", gson.toJson(credentials));

        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("authToken")) {
            communicator.setAuthToken((String) responseMap.get("authToken"));
            return true;
        } else {
            throw new Exception("Registration failed: " + responseMap.getOrDefault("message", "Unknown error"));
        }
    }

    public boolean logout() throws Exception {
        String response = communicator.sendDeleteRequest("/session");

        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("success") && (Boolean) responseMap.get("success")) {
            communicator.clearAuthToken();
            return true;
        } else {
            throw new Exception("Logout failed: " + responseMap.getOrDefault("message", "Unknown error"));
        }
    }

    public Integer createGame(String gameName) throws Exception {
        Map<String, String> gameData = Map.of("gameName", gameName);
        String response = communicator.sendPostRequest("/game", gson.toJson(gameData));

        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("gameID")) {
            return ((Double) responseMap.get("gameID")).intValue();
        } else {
            throw new Exception("Create game failed: " + responseMap.getOrDefault("message", "Unknown error"));
        }
    }

    public Map<Integer, String> listGames() throws Exception {
        String response = communicator.sendGetRequest("/game");
        Map<Integer, String> games = gson.fromJson(response, Map.class);
        return games;
    }

    public boolean joinGame(int gameId, String color) throws Exception {
        Map<String, Object> gameData = Map.of("gameID", gameId, "playerColor", color);
        String response = communicator.sendPutRequest("/game", gson.toJson(gameData));

        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("success") && (Boolean) responseMap.get("success")) {
            return true;
        } else {
            throw new Exception("Join game failed: " + responseMap.getOrDefault("message", "Unknown error"));
        }
    }

    public String observeGame(int gameId) throws Exception {
        String response = communicator.sendGetRequest("/game/observe/" + gameId);

        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("gameState")) {
            return (String) responseMap.get("gameState");
        } else {
            throw new Exception("Observe game failed: " + responseMap.getOrDefault("message", "Unknown error"));
        }
    }
}
