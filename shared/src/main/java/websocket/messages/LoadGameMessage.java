package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {
    private ChessGame game;

    public LoadGameMessage() {
        super(ServerMessageType.LOAD_GAME);
    }

    public ChessGame getChessGame() {
        return game;
    }
}
