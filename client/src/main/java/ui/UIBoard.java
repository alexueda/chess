package ui;

import chess.*;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static ui.EscapeSequences.*;

public class UIBoard {
    private final ChessBoard chessBoard;

    public UIBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    public void printBoardWhiteBottom() {
        printBoard(true, null);  // White at the bottom, no highlights
    }

    public void printBoardBlackBottom() {
        printBoard(false, null);  // Black at the bottom, no highlights
    }

    public void printBoardWhiteHL(List<ChessPosition> highlights) {
        printBoard(true, highlights);  // White at the bottom, with highlights
    }

    public void printBoardBlackHL(List<ChessPosition> highlights) {
        printBoard(false, highlights);  // Black at the bottom, with highlights
    }

    private void printBoard(boolean whiteBottom, List<ChessPosition> highlights) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        out.print("   ");

        // Print column headers
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

        int startRow = whiteBottom ? 8 : 1;
        int endRow = whiteBottom ? 1 : 8;
        int rowIncrement = whiteBottom ? -1 : 1;

        // Print each row of the board
        for (int row = startRow; row != endRow + rowIncrement; row += rowIncrement) {
            out.print(SET_TEXT_COLOR_WHITE + " " + row + " " + RESET_TEXT_COLOR);

            for (int col = 1; col <= 8; col++) {
                int actualCol = whiteBottom ? col : 9 - col;  // Reverse columns for Black's perspective
                ChessPosition position = new ChessPosition(row, actualCol);
                ChessPiece piece = chessBoard.getPiece(position);
                String pieceSymbol = getPieceSymbol(piece);

                // Highlight squares if they are in the highlights list
                boolean isHighlighted = highlights != null && highlights.contains(position);
                boolean isLightSquare = (row + actualCol) % 2 == 1;
                String bgColor = isHighlighted
                        ? SET_BG_COLOR_GREEN // Highlight color
                        : (isLightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY);

                out.print(bgColor + " " + pieceSymbol + " " + RESET_BG_COLOR);
            }

            out.print(SET_TEXT_COLOR_WHITE + " " + row + RESET_TEXT_COLOR);
            out.println();
        }

        // Print column headers again
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

        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                ? SET_TEXT_COLOR_BLUE
                : SET_TEXT_COLOR_RED;

        return color + symbol + RESET_TEXT_COLOR;
    }
}
