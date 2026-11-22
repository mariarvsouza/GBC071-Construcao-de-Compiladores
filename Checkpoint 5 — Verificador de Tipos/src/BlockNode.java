import java.util.List;
import java.util.ArrayList;

public class BlockNode extends StmtNode {
    public final List<StmtNode> statements;

    public BlockNode(List<StmtNode> statements, int line, int col) {
        super(line, col);
        if (statements != null) {
            this.statements = statements;
        } else {
            this.statements = new ArrayList<>();
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBlock(this);
    }
}