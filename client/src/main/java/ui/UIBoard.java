package ui;

import chess.*;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import static ui.EscapeSequences.*;

public class UIBoard {
    private final ChessBoard chessBoard;

    public UIBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    public void printBoard() {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        out.print("   ");
        for (char col = 'a'; col <= 'h'; col++) {
            out.print(SET_TEXT_COLOR_WHITE + " " + col + " " + RESET_TEXT_COLOR);
        }
        out.println();
        for (int row = 8; row >= 1; row--) {
            out.print(SET_TEXT_COLOR_WHITE + " " + row + " " + RESET_TEXT_COLOR);  // Row label on the left
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, col));
                String pieceSymbol = getPieceSymbol(piece);
                boolean isLightSquare = (row + col) % 2 == 1;
                String bgColor = isLightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
                out.print(bgColor + " " + pieceSymbol + " " + RESET_BG_COLOR);
            }
            out.print(SET_TEXT_COLOR_WHITE + " " + row + RESET_TEXT_COLOR);
            out.println();
        }
        out.print("   ");
        for (char col = 'a'; col <= 'h'; col++) {
            out.print(SET_TEXT_COLOR_WHITE + " " + col + " " + RESET_TEXT_COLOR);
        }
        out.println();
    }

    private String getPieceSymbol(ChessPiece piece) {
        if (piece == null) {
            return " ";
        }

        String symbol;
        switch (piece.getPieceType()) {
            case KING -> symbol = "K";
            case QUEEN -> symbol = "Q";
            case ROOK -> symbol = "R";
            case BISHOP -> symbol = "B";
            case KNIGHT -> symbol = "N";
            case PAWN -> symbol = "P";
            default -> symbol = " ";
        }
        String color = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_BLUE : SET_TEXT_COLOR_RED;
        return color + symbol + RESET_TEXT_COLOR;
    }
}
