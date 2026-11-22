/*
 * Classe base para todas as expressoes da AST.
 */

public abstract class ExprNode extends StmtNode {
    public Type type;
    public ExprNode(int line, int col) {
        super(line, col);
        this.type = null;
    }
}
