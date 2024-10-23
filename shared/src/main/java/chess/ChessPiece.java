package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    public PieceType getPieceType() {
        return this.type;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMove = new ArrayList<>();
        switch (this.type) {
            case PAWN -> pawnMove(board, myPosition, validMove);
            case ROOK -> rookMove(board, myPosition, validMove);
            case KNIGHT -> knightMove(board, myPosition, validMove);
            case BISHOP -> bishopMove(board, myPosition, validMove);
            case QUEEN -> {
                rookMove(board, myPosition, validMove); // Queen moves like both rook and bishop
                bishopMove(board, myPosition, validMove);
            }
            case KING -> kingMove(board, myPosition, validMove);
        }
        return validMove;
    }

    private boolean inBounds(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    private void handlePromotion(ChessPosition myPosition, ChessPosition targetPosition, Collection<ChessMove> validMove) {
        validMove.add(new ChessMove(myPosition, targetPosition, PieceType.QUEEN));
        validMove.add(new ChessMove(myPosition, targetPosition, PieceType.ROOK));
        validMove.add(new ChessMove(myPosition, targetPosition, PieceType.KNIGHT));
        validMove.add(new ChessMove(myPosition, targetPosition, PieceType.BISHOP));
    }

    private void pawnMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        int moveDirection = (this.pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        boolean promotion = (this.pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7) ||
                (this.pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2);

        ChessPosition forward = new ChessPosition(myPosition.getRow() + moveDirection, myPosition.getColumn());
        if (inBounds(forward) && board.getPiece(forward) == null) {
            if (promotion) {
                handlePromotion(myPosition, forward, validMove);
            } else {
                validMove.add(new ChessMove(myPosition, forward, null));
            }
        }
        checkPawnCapture(board, myPosition, validMove, moveDirection, -1, promotion);
        checkPawnCapture(board, myPosition, validMove, moveDirection, 1, promotion);

        if ((this.pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2) ||
                (this.pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)) {
            ChessPosition twoMovesForward = new ChessPosition(myPosition.getRow() + 2 * moveDirection, myPosition.getColumn());
            if (board.getPiece(forward) == null && board.getPiece(twoMovesForward) == null) {
                validMove.add(new ChessMove(myPosition, twoMovesForward, null));
            }
        }
    }

    private void checkPawnCapture(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove,
                                  int rowOffset, int colOffset, boolean promotion) {
        ChessPosition capturePosition = new ChessPosition(myPosition.getRow() + rowOffset, myPosition.getColumn() + colOffset);
        if (inBounds(capturePosition) && board.getPiece(capturePosition) != null &&
                board.getPiece(capturePosition).getTeamColor() != this.pieceColor) {
            if (promotion) {
                handlePromotion(myPosition, capturePosition, validMove);
            } else {
                validMove.add(new ChessMove(myPosition, capturePosition, null));
            }
        }
    }

    private void rookMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        checkMovesInMultipleDirections(board, myPosition, validMove, new int[][]{
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        });
    }

    private void bishopMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        checkMovesInMultipleDirections(board, myPosition, validMove, new int[][]{
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        });
    }

    private void kingMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        int[][] kingMoves = {
                {0, 1}, {1, 1}, {1, 0}, {1, -1},
                {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
        };
        checkPredefinedMoves(board, myPosition, validMove, kingMoves);
    }

    private void knightMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        int[][] knightMoves = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        checkPredefinedMoves(board, myPosition, validMove, knightMoves);
    }

    private void checkPredefinedMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove, int[][] predefinedMoves) {
        for (int[] move : predefinedMoves) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + move[0], myPosition.getColumn() + move[1]);
            if (inBounds(newPosition) && (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != this.pieceColor)) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            }
        }
    }

    private void checkMovesInMultipleDirections(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove, int[][] directions) {
        for (int[] direction : directions) {
            checkMovesInDirection(board, myPosition, validMove, direction[0], direction[1]);
        }
    }

    private void checkMovesInDirection(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove, int rowOffset, int colOffset) {
        for (int r = myPosition.getRow() + rowOffset, c = myPosition.getColumn() + colOffset;
             inBounds(new ChessPosition(r, c)); r += rowOffset, c += colOffset) {
            ChessPosition newPosition = new ChessPosition(r, c);
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
    }
}
