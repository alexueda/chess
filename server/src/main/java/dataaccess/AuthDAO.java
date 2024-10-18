package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {

    private Map<String, AuthData> authTable = new HashMap<>();

    public void insertAuth(AuthData auth) {
        authTable.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String authToken) {
        return authTable.get(authToken);
    }

    public void deleteAuth(String authToken) {
        authTable.remove(authToken);
    }

    public void clearAuth() {
        authTable.clear();
    }
}