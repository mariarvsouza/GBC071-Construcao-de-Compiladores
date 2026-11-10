import java.util.List;
import java.util.ArrayList;

public class ForStmtNode extends StmtNode {
    public final StmtNode init; //VarDeclNode ou AssignNode
    public final ExprNode condition;
    public final AssignNode increment;
    public final BlockNode body;

    public ForStmtNode(StmtNode init, ExprNode condition, AssignNode increment, BlockNode body, int line, int col) {
        super(line, col);
        this.init = init;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitForStmt(this);
    }
}