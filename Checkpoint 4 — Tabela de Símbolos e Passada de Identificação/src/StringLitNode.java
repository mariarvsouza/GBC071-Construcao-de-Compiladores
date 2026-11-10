import java.util.List;
import java.util.ArrayList;

public class StringLitNode extends ExprNode {
    public final String value;

    public StringLitNode(String value, int line, int col) {
        super(line, col);
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitStringLit(this);
    }
}