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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
            case QUEEN -> queenMove(board, myPosition, validMove);
            case KING -> kingMove(board, myPosition, validMove);
        }
        return validMove;
    }

    private boolean inBound(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    private void helpPromotion(ChessPosition myPosition, ChessPosition targetPosition, Collection<ChessMove> validMove) {
        validMove.add(new ChessMove(myPosition, targetPosition, PieceType.QUEEN));
        validMove.add(new ChessMove(myPosition, targetPosition, PieceType.ROOK));
        validMove.add(new ChessMove(myPosition, targetPosition, PieceType.KNIGHT));
        validMove.add(new ChessMove(myPosition, targetPosition, PieceType.BISHOP));
    }

    private void pawnMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        int oneMove = this.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;
        boolean promotion = (this.pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7) ||
                (this.pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2);

        ChessPosition forward = new ChessPosition(myPosition.getRow() + oneMove, myPosition.getColumn());
        if (inBound(forward) && board.getPiece(forward) == null) {
            if (promotion) {
                helpPromotion(myPosition, forward, validMove);
            } else {
                validMove.add(new ChessMove(myPosition, forward, null));
            }
        }
        checkPawnCapture(board, myPosition, validMove, oneMove, -1, promotion);
        checkPawnCapture(board, myPosition, validMove, oneMove, 1, promotion);

        if ((this.pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2) ||
                (this.pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)) {
            ChessPosition twoMove = new ChessPosition(myPosition.getRow() + 2 * oneMove, myPosition.getColumn());
            if (board.getPiece(forward) == null && board.getPiece(twoMove) == null) {
                validMove.add(new ChessMove(myPosition, twoMove, null));
            }
        }
    }

    private void checkPawnCapture(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove, int rowOffset, int colOffset, boolean promotion) {
        ChessPosition capture = new ChessPosition(myPosition.getRow() + rowOffset, myPosition.getColumn() + colOffset);
        if (inBound(capture) && board.getPiece(capture) != null && board.getPiece(capture).getTeamColor() != this.pieceColor) {
            if (promotion) {
                helpPromotion(myPosition, capture, validMove);
            } else {
                validMove.add(new ChessMove(myPosition, capture, null));
            }
        }
    }

    private void rookMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        checkLinearMoves(board, myPosition, validMove, 1, 0);
        checkLinearMoves(board, myPosition, validMove, -1, 0);
        checkLinearMoves(board, myPosition, validMove, 0, 1);
        checkLinearMoves(board, myPosition, validMove, 0, -1);
    }

    private void bishopMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        checkDiagonalMoves(board, myPosition, validMove, 1, 1);
        checkDiagonalMoves(board, myPosition, validMove, 1, -1);
        checkDiagonalMoves(board, myPosition, validMove, -1, 1);
        checkDiagonalMoves(board, myPosition, validMove, -1, -1);
    }

    private void queenMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        rookMove(board, myPosition, validMove);
        bishopMove(board, myPosition, validMove);
    }

    private void kingMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        int[][] kingMoves = {
                {0, 1}, {1, 1}, {1, 0}, {1, -1},
                {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
        };
        for (int[] move : kingMoves) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + move[0], myPosition.getColumn() + move[1]);
            if (inBound(newPosition) && (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != this.pieceColor)) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            }
        }
    }

    private void knightMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        int[][] knightMoves = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        for (int[] move : knightMoves) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + move[0], myPosition.getColumn() + move[1]);
            if (inBound(newPosition) && (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != this.pieceColor)) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            }
        }
    }

    private void checkLinearMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove, int rowOffset, int colOffset) {
        for (int r = myPosition.getRow() + rowOffset, c = myPosition.getColumn() + colOffset;
             inBound(new ChessPosition(r, c)); r += rowOffset, c += colOffset) {
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

    private void checkDiagonalMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove, int rowOffset, int colOffset) {
        for (int r = myPosition.getRow() + rowOffset, c = myPosition.getColumn() + colOffset;
             inBound(new ChessPosition(r, c)); r += rowOffset, c += colOffset) {
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