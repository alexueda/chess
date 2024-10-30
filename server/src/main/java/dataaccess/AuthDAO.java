package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {

    private Map<String, AuthData> authTable = new HashMap<>();

    public void insertAuth(AuthData authData) {
        authTable.put(authData.authToken(), authData);
    }

    public AuthData getAuth(String authToken) {
        return authTable.get(authToken);
    }

    public void deleteAuth(String authToken) {
        authTable.remove(authToken);
    }

    public void clearAuths() {
        authTable.clear();
    }
}