public class Symbol {
    public final String name;
    public final Kind kind;
    public final Node decl;
    public final int level;

    // Campos reservados para os checkpoints seguintes.
    public Type type;
    public int offset;

    public Symbol(String name, Kind kind, Node decl, int level) {
        this.name = name;
        this.kind = kind;
        this.decl = decl;
        this.level = level;
        this.type = null;
        this.offset = -1;
    }
}
