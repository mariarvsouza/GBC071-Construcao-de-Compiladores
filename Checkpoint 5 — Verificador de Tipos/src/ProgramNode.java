import java.util.List;
import java.util.ArrayList;

public class ProgramNode extends Node {
    public final List<StmtNode> statements;

    public ProgramNode(List<StmtNode> statements, int line, int col) {
        super(line, col);
        if (statements != null) {
            this.statements = statements;
        } else {
            this.statements = new ArrayList<>();
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitProgram(this);
    }
}