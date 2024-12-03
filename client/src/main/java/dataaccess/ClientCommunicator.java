package dataaccess;

import java.io.*;
import java.net.*;

public class ClientCommunicator {

    private final String baseUrl;
    private String authToken;

    public ClientCommunicator(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void clearAuthToken() {
        this.authToken = null;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String sendGetRequest(String endpoint) throws IOException {
        HttpURLConnection connection = createConnection(endpoint, "GET");
        return readResponse(connection);
    }

    public String sendPostRequest(String endpoint, String payload) throws IOException {
        HttpURLConnection connection = createConnection(endpoint, "POST");
        writePayload(connection, payload);
        return readResponse(connection);
    }

    public String sendPutRequest(String endpoint, String payload) throws IOException {
        HttpURLConnection connection = createConnection(endpoint, "PUT");
        writePayload(connection, payload);
        return readResponse(connection);
    }

    public String sendDeleteRequest(String endpoint) throws IOException {
        HttpURLConnection connection = createConnection(endpoint, "DELETE");
        return readResponse(connection);
    }

    private HttpURLConnection createConnection(String endpoint, String method) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            connection.setRequestProperty("Authorization", authToken);
        }
        connection.setDoOutput("POST".equals(method) || "PUT".equals(method));
        return connection;
    }

    private void writePayload(HttpURLConnection connection, String payload) throws IOException {
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(payload.getBytes());
            outputStream.flush();
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("HTTP error code: " + responseCode + " - " + response.toString());
        }
        return response.toString();
    }
}
