import java.util.List;
import java.util.ArrayList;

public class IfStmtNode extends StmtNode {
    public final ExprNode condition;
    public final BlockNode thenBlock;
    public final BlockNode elseBlock; 

    public IfStmtNode(ExprNode condition, BlockNode thenBlock, BlockNode elseBlock, int line, int col) {
        super(line, col);
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }
}