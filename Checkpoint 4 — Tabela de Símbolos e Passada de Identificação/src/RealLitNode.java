import java.util.List;
import java.util.ArrayList;

public class RealLitNode extends ExprNode {
    public final double value;

    public RealLitNode(double value, int line, int col) {
        super(line, col);
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitRealLit(this);
    }
}