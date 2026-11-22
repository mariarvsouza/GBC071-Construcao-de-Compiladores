import java.util.List;
import java.util.ArrayList;

public class UnaryExprNode extends ExprNode {
    public final String operator;
    public final ExprNode operand;

    public UnaryExprNode(String operator, ExprNode operand, int line, int col) {
        super(line, col);
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}