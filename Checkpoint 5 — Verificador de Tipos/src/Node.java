public abstract class Node {
    public final int line;
    public final int col;

    public Node(int line, int col) {
        this.line = line;
        this.col = col;
    }

    public abstract <T> T accept(Visitor<T> visitor);
}
