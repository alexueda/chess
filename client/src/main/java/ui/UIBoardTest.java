package ui;

import chess.ChessBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UIBoardTest {

    private ChessBoard chessBoard;
    private UIBoard uiBoard;

    @BeforeEach
    public void setUp() {
        chessBoard = new ChessBoard();
        chessBoard.resetBoard();  // Set up the initial positions for pieces
        uiBoard = new UIBoard(chessBoard);
    }

    @Test
    public void testPrintBoardWhiteBottom() {
        System.out.println("Testing board with White at bottom:");
        uiBoard.printBoardWhiteBottom();
    }

    @Test
    public void testPrintBoardBlackBottom() {
        System.out.println("Testing board with Black at bottom:");
        uiBoard.printBoardBlackBottom();
    }
}
