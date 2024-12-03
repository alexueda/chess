package service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dataaccess.ClientCommunicator;
import model.GameData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class ServerFacade {
    private final ClientCommunicator communicator;
    private final Gson gson = new Gson();

    public ServerFacade(String baseUrl) {
        this.communicator = new ClientCommunicator(baseUrl);
    }

    public boolean login(String username, String password) {
        Map<String, String> credentials = Map.of("username", username, "password", password);
        try {
            String response = communicator.sendPostRequest("/session", gson.toJson(credentials));
            Map<String, Object> responseMap = gson.fromJson(response, Map.class);
            if (responseMap.containsKey("authToken")) {
                communicator.setAuthToken((String) responseMap.get("authToken"));
                return true;
            }
        } catch (IOException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
        return false;
    }

    public boolean register(String username, String password, String email) throws IOException {
        Map<String, String> credentials = Map.of("username", username, "password", password, "email", email);
        String response = communicator.sendPostRequest("/user", gson.toJson(credentials));
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("authToken")) {
            communicator.setAuthToken((String) responseMap.get("authToken"));
            return true;
        }
        return false;
    }

    public boolean logout() throws IOException {
        String response = communicator.sendDeleteRequest("/session");
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if ("Successfully logged out".equals(responseMap.get("message"))) {
            communicator.clearAuthToken();
            return true;
        }
        return false;
    }

    public boolean createGame(String gameName) throws IOException {
        Map<String, String> gameData = Map.of("gameName", gameName, "gameID", gameName);
        String response = communicator.sendPostRequest("/game", gson.toJson(gameData));
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        return responseMap.containsKey("gameID");
    }

    public List<GameData> listGames() throws IOException {
        String response = communicator.sendGetRequest("/game");
        Type responseType = new TypeToken<Map<String, List<GameData>>>() {}.getType();
        Map<String, List<GameData>> responseMap = gson.fromJson(response, responseType);
        return responseMap.get("games");
    }

    public boolean joinGame(int gameId, String color) throws IOException {
        Map<String, Object> gameData = Map.of("gameID", gameId, "playerColor", color);
        String response = communicator.sendPutRequest("/game", gson.toJson(gameData));
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        return responseMap.isEmpty();
    }

    public boolean clearDatabase() throws IOException {
        String response = communicator.sendDeleteRequest("/db");
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        return responseMap != null && responseMap.isEmpty();
    }

    public String getAuthToken() {
        return communicator.getAuthToken();
    }

    public void clearAuthToken() {
        communicator.clearAuthToken();
    }
}
