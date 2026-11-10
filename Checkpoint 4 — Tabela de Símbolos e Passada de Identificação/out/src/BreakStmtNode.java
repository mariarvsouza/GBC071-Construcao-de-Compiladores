import java.util.List;
import java.util.ArrayList;

public class BreakStmtNode extends StmtNode {
    public BreakStmtNode(int line, int col) {
        super(line, col);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBreakStmt(this);
    }
}