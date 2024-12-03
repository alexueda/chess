package websocket.commands;

public class UserGameCommand {
    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    private CommandType commandType;
    private String authToken;
    private Integer gameID;
    private Object move;  // Only used for MAKE_MOVE

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, Object move) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.move = move;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public Object getMove() {
        return move;
    }
}
