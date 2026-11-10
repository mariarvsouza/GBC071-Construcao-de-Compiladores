import java.util.List;
import java.util.ArrayList;

public class AssignNode extends StmtNode {
    public final String varID;
    public final ExprNode expr;

    public AssignNode(String varID, ExprNode expr, int line, int col) {
        super(line, col);
        this.varID = varID;
        this.expr = expr;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitAssign(this);
    }
}