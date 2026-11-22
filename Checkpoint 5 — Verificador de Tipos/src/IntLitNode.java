import java.util.List;
import java.util.ArrayList;

public class IntLitNode extends ExprNode {
    public final int value;

    public IntLitNode(int value, int line, int col) {
        super(line, col);
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitIntLit(this);
    }
}