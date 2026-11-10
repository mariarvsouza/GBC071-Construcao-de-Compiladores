import java.util.List;
import java.util.ArrayList;

public class ReturnStmtNode extends StmtNode {
    public final ExprNode expr;

    public ReturnStmtNode(ExprNode expr, int line, int col) {
        super(line, col);
        this.expr = expr;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitReturnStmt(this);
    }
}