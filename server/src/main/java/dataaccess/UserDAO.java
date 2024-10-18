package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {

    private Map<String, UserData> userTable = new HashMap<>();

    public void insertUser(UserData user) {
        userTable.put(user.username(), user);
    }

    public UserData getUser(String username) {
        return userTable.get(username);
    }

    public void clearUsers() {
        userTable.clear();
    }
}