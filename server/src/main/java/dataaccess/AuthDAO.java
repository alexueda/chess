package dataaccess;

import model.AuthData;

public interface AuthDAO {
    AuthData getAuth(String authToken) throws DataAccessException;
    void insertAuth(AuthData authData) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void clearAuths() throws DataAccessException;
}
