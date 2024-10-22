package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private final Map<String, UserData> userTable = new HashMap<>();

    public UserData getUser(String username) {
        return userTable.get(username);
    }

    public void insertUser(UserData user) {
        userTable.put(user.username(), user);
    }

    public void clearUsers() {
        userTable.clear();
    }
}