package MyStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

public class ArrayRope implements CharSequence {
    final static private int INITIAL_CAPACITY = 16;

    private static class ArrayRopePiece implements CharSequence {
        int start;
        int end;
        final CharSequence content;
        ArrayRopePiece(int start, int end, CharSequence content) {
            this.start = start;
            this.end = end;
            if (content instanceof ArrayRopePiece) {
                this.start += ((ArrayRopePiece) content).start;
                this.end += ((ArrayRopePiece) content).start;
                this.content = ((ArrayRopePiece) content).content;
            } else {
                this.content = content;
            }
        }
        ArrayRopePiece(CharSequence content) {
            this(0, content.length(), content);
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public char charAt(int index) {
            return content.charAt(start + index);
        }

        @Override
        public boolean isEmpty() {
            return this.content.isEmpty() || this.length() == 0;
        }

        @Override
        public ArrayRopePiece subSequence(int start, int end) {
            return new ArrayRopePiece(this.start + start, this.start + end, content);
        }

        @Override
        public IntStream chars() {
            // TODO: support chars() properly
            return this.content.subSequence(this.start, this.end).chars();
        }

        @Override
        public IntStream codePoints() {
            // TODO: support codePoints() properly
            return this.content.subSequence(this.start, this.end).codePoints();
        }
    }

    private final ArrayList<ArrayRopePiece> pieces;
    private final ArrayList<Integer> indices;
    @Override
    public int length() {
        int last_piece_index = pieces.size() - 1;
        if (last_piece_index >= 0) {
            ArrayRopePiece piece = pieces.get(last_piece_index);
            return piece.end - piece.start + indices.get(last_piece_index);
        }
        return 0;
    }

    @Override
    public char charAt(int index) {
        int piece_index = Collections.binarySearch(this.indices, index);
        if (piece_index < 0) {
            piece_index = -piece_index - 2;
        }
        return pieces.get(piece_index).charAt(index - this.indices.get(piece_index));
    }

    @Override
    public boolean isEmpty() {
        return this.pieces.isEmpty();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
//        find the first and last piece
        int start_piece_index = Collections.binarySearch(this.indices, start);
        if (start_piece_index < 0) {
            start_piece_index = -start_piece_index - 2;
        }
        int end_piece_index = Collections.binarySearch(this.indices.subList(start_piece_index, this.indices.size()), end);
        if (end_piece_index < 0) {
            end_piece_index = -end_piece_index - 2;
        }
        end_piece_index += start_piece_index;
//        locate subsequence in start and end pieces
        int char_index_in_start_piece = start - indices.get(start_piece_index);
        int char_index_in_end_piece = end - indices.get(end_piece_index);
        if (start_piece_index == end_piece_index) {
            return this.pieces.get(start_piece_index).subSequence(char_index_in_start_piece, char_index_in_end_piece);
        }
//        Assemble new pieces collection
        ArrayRopePiece[] subPieces = new ArrayRopePiece[end_piece_index - start_piece_index + 1];
        ArrayRopePiece first = this.pieces.get(start_piece_index);
        subPieces[0] = first.subSequence(char_index_in_start_piece, first.length());
        for (int i = start_piece_index + 1; i < end_piece_index; i++) {
            subPieces[i - start_piece_index] = pieces.get(start_piece_index);
        }
        ArrayRopePiece last = this.pieces.get(end_piece_index);
        subPieces[subPieces.length - 1] = last.subSequence(0, char_index_in_end_piece);
        return new ArrayRope(subPieces);
    }


    public ArrayRope() {
        this.pieces = new ArrayList<ArrayRopePiece>(INITIAL_CAPACITY);
        this.indices = new ArrayList<>(INITIAL_CAPACITY);
    }
    private ArrayRope(ArrayList<ArrayRopePiece> pieces, ArrayList<Integer> indices) {
        this.pieces = pieces;
        this.indices = indices;
    }
    public ArrayRope(CharSequence... contents) {
        this();
        for (CharSequence cs : contents) {
            this.append(cs);
        }
    }

    public ArrayRope append(CharSequence cs) {
        int origin_len = this.length();
        if (cs.isEmpty()) {
            return this;
        }
        if (cs instanceof ArrayRope appended) {
            this.indices.addAll(appended.indices.stream().map(x -> x + origin_len).toList());
            this.pieces.addAll(appended.pieces);
        } else if (cs instanceof ArrayRopePiece piece) {
            this.indices.add(origin_len);
            this.pieces.add(piece);
        }else{
            this.indices.add(origin_len);
            this.pieces.add(new ArrayRopePiece(cs));
        }
        return this;
    }

    public ArrayRope concat(CharSequence cs) {
        ArrayList<ArrayRopePiece> ori_pieces = (ArrayList<ArrayRopePiece>) this.pieces.clone();
        ArrayList<Integer> ori_indices = (ArrayList<Integer>) this.indices.clone();
        ArrayRope new_rope = new ArrayRope(ori_pieces, ori_indices);
        new_rope.append(cs);
        return new_rope;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ArrayRopePiece piece : this.pieces) {
//            sb.append(piece);
            sb.append(piece.content, piece.start, piece.end);
        }
        return sb.toString();
    }

    @Override
    public IntStream chars() {
        return CharSequence.super.chars();
    }

    @Override
    public IntStream codePoints() {
        return CharSequence.super.codePoints();
    }

}
