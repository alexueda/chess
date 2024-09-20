package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
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

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */




    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMove = new ArrayList<>();
        //return each pieces valid moves
        if (this.type == PieceType.PAWN) {
            PawnMove(board, myPosition, validMove);
        } else if (this.type == PieceType.ROOK) {
            RookMove(board, myPosition, validMove);
        } else if (this.type == PieceType.KNIGHT) {
            KnightMove(board, myPosition, validMove);
        } else if (this.type == PieceType.BISHOP) {
            BishopMove(board, myPosition, validMove);
        } else if (this.type == PieceType.QUEEN) {
            QueenMove(board, myPosition, validMove);
        } else if (this.type == PieceType.KING) {
            KingMove(board, myPosition, validMove);
        }
        return validMove;
    }
    //Bounds check
    private boolean InBound(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }
    //helper method to take care the PawnPromotion
    private void helpPromotion(ChessPosition myPosition, ChessPosition targetPosition, Collection<ChessMove> validMove) {
        validMove.add(new ChessMove(myPosition, targetPosition, ChessPiece.PieceType.QUEEN));
        validMove.add(new ChessMove(myPosition, targetPosition, ChessPiece.PieceType.ROOK));
        validMove.add(new ChessMove(myPosition, targetPosition, ChessPiece.PieceType.KNIGHT));
        validMove.add(new ChessMove(myPosition, targetPosition, ChessPiece.PieceType.BISHOP));
    }

    //PAWN Method
    private void PawnMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        int oneMove; //move one up(WHITE) and down(BLACK)
        if (this.pieceColor == ChessGame.TeamColor.WHITE) {
            oneMove = 1;
        } else {
            oneMove = -1;
        }
        //check promotion or not
        boolean promotion = (this.pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7) ||
                (this.pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2);
        //basic forward move
        ChessPosition forward = new ChessPosition(myPosition.getRow() + oneMove, myPosition.getColumn());
        if (InBound(forward) && board.getPiece(forward) == null) {
            if (promotion) {
                helpPromotion(myPosition, forward, validMove);
            } else {
                validMove.add(new ChessMove(myPosition, forward, null));
            }
        }
        //left diagonal enemy
        ChessPosition leftEnemy = new ChessPosition(myPosition.getRow() + oneMove, myPosition.getColumn() - 1);
        if (InBound(leftEnemy) && board.getPiece(leftEnemy) != null && board.getPiece(leftEnemy).getTeamColor() != this.pieceColor) {
            if (promotion) {
                helpPromotion(myPosition, leftEnemy, validMove);
            } else {
                validMove.add(new ChessMove(myPosition, leftEnemy, null));
            }
        }
        //right diagonal enemy
        ChessPosition rightEnemy = new ChessPosition(myPosition.getRow() + oneMove, myPosition.getColumn() + 1);
        if (InBound(rightEnemy) && board.getPiece(rightEnemy) != null && board.getPiece(rightEnemy).getTeamColor() != this.pieceColor) {
            if (promotion) {
                helpPromotion(myPosition, rightEnemy, validMove);
            } else {
                validMove.add(new ChessMove(myPosition, rightEnemy, null));
            }
        }
        // first two forward move option
        if ((this.pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2) || (this.pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)) {
            ChessPosition twoMove = new ChessPosition(myPosition.getRow() + 2 * oneMove, myPosition.getColumn()); //* multiple by oneMove to identify WHITE move or Black Move
            if (board.getPiece(forward) == null && board.getPiece(twoMove) == null) {
                validMove.add(new ChessMove(myPosition, twoMove, null));
            }
        }
    }

    private void RookMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        //move to right
        for (int c = myPosition.getColumn() + 1; c <= 8; c++) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow(), c);
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move to left
        for (int c = myPosition.getColumn() - 1; c >= 1; c--) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow(), c);
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move up
        for (int r = myPosition.getRow() + 1; r <= 8; r++) {
            ChessPosition newPosition = new ChessPosition (r, myPosition.getColumn());
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move down
        for (int r = myPosition.getRow() - 1; r >= 1; r--) {
            ChessPosition newPosition = new ChessPosition (r, myPosition.getColumn());
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

    private void KnightMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
    //Knight move collection
        int[][] knMove = {
                {2,1}, {2,-1}, {-2,1}, {-2,-1}, {1,2},{-1,2}, {1,-2}, {-1,-2}
        };
        for (int[] move : knMove) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + move[0], myPosition.getColumn() + move[1]);
            if (InBound(newPosition) && (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != this.pieceColor))
                validMove.add(new ChessMove(myPosition, newPosition, null));
        }
    }
    private void BishopMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        //move to right up diagnal
        for (int i = 1; i <= 8; i++) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow() + i, myPosition.getColumn() + i);
            if (!InBound(newPosition)) {
                break;
            }
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move to left up diagnal
        for (int i = 1; i <= 8; i++) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow() + i, myPosition.getColumn() - i);
            if (!InBound(newPosition)) {
                break;
            }
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move to right down diagnal
        for (int i = 1; i <= 8; i++) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow() - i, myPosition.getColumn() + i);
            if (!InBound(newPosition)) {
                break;
            }
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move to left down diagnal
        for (int i = 1; i <= 8; i++) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow() - i, myPosition.getColumn() - i);
            if (!InBound(newPosition)) {
                break;
            }
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
    private void QueenMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        RookMove(board, myPosition, validMove);
        BishopMove(board, myPosition, validMove);
    }

    private void KingMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        int[][] kiMove = {
                {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
        };
        for (int[] move : kiMove) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + move[0], myPosition.getColumn() + move[1]);
            if (InBound(newPosition) && (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != this.pieceColor))
                validMove.add(new ChessMove(myPosition, newPosition, null));
        }
    }
}

