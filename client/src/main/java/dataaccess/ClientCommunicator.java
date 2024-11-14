package dataaccess;

import java.io.*;
import java.net.*;

public class ClientCommunicator {
    private final String baseUrl;
    private String authToken = null;

    public ClientCommunicator(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void clearAuthToken() {
        this.authToken = null;
    }

    public String sendGetRequest(String endpoint) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, "GET");
        return readResponse(conn);
    }

    public String sendPostRequest(String endpoint, String payload) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, "POST");
        writePayload(conn, payload);
        return readResponse(conn);
    }

    public String sendPutRequest(String endpoint, String payload) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, "PUT");
        writePayload(conn, payload);
        return readResponse(conn);
    }

    public String sendDeleteRequest(String endpoint) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, "DELETE");
        return readResponse(conn);
    }

    private HttpURLConnection createConnection(String endpoint, String method) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            conn.setRequestProperty("Authorization", authToken);
        }
        conn.setDoOutput("POST".equals(method) || "PUT".equals(method));
        return conn;
    }

    private void writePayload(HttpURLConnection conn, String payload) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
            os.flush();
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        InputStream inputStream = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();

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