/** Comando print(Expr); */
public final class PrintStmtNode extends StmtNode {
    public final ExprNode expr;

    public PrintStmtNode(ExprNode expr, int line, int col) {
        super(line, col);
        this.expr = expr;
    }

    @Override public <T> T accept(Visitor<T> visitor) {
        return visitor.visitPrintStmt(this);
    }
}
