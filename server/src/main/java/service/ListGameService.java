package service;

import dataaccess.GameDAO;
import model.GameData;

import java.util.ArrayList;
import java.util.List;

public class ListGameService {

    private final GameDAO gameDAO;

    public ListGameService() {
        this.gameDAO = new GameDAO();
    }

    public List<GameData> listAllGames() {
        // Convert Map values to List because if not this code will throw error
        return new ArrayList<>(gameDAO.getAllGames().values());
    }
}
