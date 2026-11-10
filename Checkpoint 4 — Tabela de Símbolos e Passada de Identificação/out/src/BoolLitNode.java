import java.util.List;
import java.util.ArrayList;

public class BoolLitNode extends ExprNode {
    public final boolean value;

    public BoolLitNode(boolean value, int line, int col) {
        super(line, col);
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBoolLit(this);
    }
}