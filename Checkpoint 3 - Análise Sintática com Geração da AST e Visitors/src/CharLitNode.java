import java.util.List;
import java.util.ArrayList;

public class CharLitNode extends ExprNode {
    public final char value;

    public CharLitNode(char value, int line, int col) {
        super(line, col);
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitCharLit(this);
    }
}