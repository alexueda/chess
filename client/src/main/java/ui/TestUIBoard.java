package ui;

import chess.ChessBoard;

public class TestUIBoard {
    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        UIBoard uiBoard = new UIBoard(board);
        uiBoard.printBoard();
    }
}
