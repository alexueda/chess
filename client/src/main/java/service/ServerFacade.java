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

    public ServerFacade(String baseUrl) {  // Accept base URL for test configuration
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
            } else {
                System.out.println("Error: " + responseMap.get("message"));
                return false;
            }
        } catch (IOException e) {
            System.out.println("Login failed with exception: " + e.getMessage());
            return false;
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
            System.out.println("Error: " + responseMap.get("message"));
            return false;
        }
    }

    public boolean logout() throws Exception {
        String response = communicator.sendDeleteRequest("/session");
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("message") && responseMap.get("message").equals("Successfully logged out")) {
            communicator.clearAuthToken();
            return true;
        } else {
            System.out.println("Error: " + responseMap.get("message"));
            return false;
        }
    }

    public boolean createGame(String gameName) throws Exception {
        Map<String, String> gameData = Map.of("gameName", gameName, "gameID", gameName);
        String response = communicator.sendPostRequest("/game", gson.toJson(gameData));
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        return responseMap.containsKey("gameID");
    }

    public List<GameData> listGames() throws Exception {
        String response = communicator.sendGetRequest("/game");
        Type responseType = new TypeToken<Map<String, List<GameData>>>(){}.getType();
        Map<String, List<GameData>> responseMap = gson.fromJson(response, responseType);
        return responseMap.get("games");
    }

    public boolean joinGame(int gameId, String color) throws Exception {
        Map<String, Object> gameData = Map.of("gameID", gameId, "playerColor", color);
        String response = communicator.sendPutRequest("/game", gson.toJson(gameData));
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.isEmpty()) {
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
            System.out.println("Error: " + responseMap.get("message"));
            return null;
        }
    }
}
