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

    public void printBoardWhiteBottom() {
        printBoard(true);  // White at the bottom
    }

    public void printBoardBlackBottom() {
        printBoard(false);  // Black at the bottom
    }

    private void printBoard(boolean whiteBottom) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        out.print("   ");

        // Column labels based on perspective
        if (whiteBottom) {
            for (char col = 'a'; col <= 'h'; col++) {
                out.print(SET_TEXT_COLOR_WHITE + " " + col + " " + RESET_TEXT_COLOR);
            }
        } else {
            for (char col = 'h'; col >= 'a'; col--) {
                out.print(SET_TEXT_COLOR_WHITE + " " + col + " " + RESET_TEXT_COLOR);
            }
        }
        out.println();

        // Define row traversal direction based on perspective
        int startRow = whiteBottom ? 8 : 1;
        int endRow = whiteBottom ? 1 : 8;
        int rowIncrement = whiteBottom ? -1 : 1;

        // Traverse each row
        for (int row = startRow; row != endRow + rowIncrement; row += rowIncrement) {
            out.print(SET_TEXT_COLOR_WHITE + " " + row + " " + RESET_TEXT_COLOR);

            for (int col = 1; col <= 8; col++) {
                int actualCol = whiteBottom ? col : 9 - col;  // Reverse columns for Black's perspective
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, actualCol));
                String pieceSymbol = getPieceSymbol(piece, whiteBottom);
                boolean isLightSquare = (row + actualCol) % 2 == 1;
                String bgColor = isLightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
                out.print(bgColor + " " + pieceSymbol + " " + RESET_BG_COLOR);
            }

            out.print(SET_TEXT_COLOR_WHITE + " " + row + RESET_TEXT_COLOR);
            out.println();
        }

        // Reprint column labels at the bottom
        out.print("   ");
        if (whiteBottom) {
            for (char col = 'a'; col <= 'h'; col++) {
                out.print(SET_TEXT_COLOR_WHITE + " " + col + " " + RESET_TEXT_COLOR);
            }
        } else {
            for (char col = 'h'; col >= 'a'; col--) {
                out.print(SET_TEXT_COLOR_WHITE + " " + col + " " + RESET_TEXT_COLOR);
            }
        }
        out.println();
    }

    private String getPieceSymbol(ChessPiece piece, boolean whiteBottom) {
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

        // Adjust piece color based on perspective
        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                ? SET_TEXT_COLOR_BLUE
                : SET_TEXT_COLOR_RED;

        return color + symbol + RESET_TEXT_COLOR;
    }
}
