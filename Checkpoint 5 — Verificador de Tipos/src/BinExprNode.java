import java.util.List;
import java.util.ArrayList;

public class BinExprNode extends ExprNode {
    public final ExprNode left;
    public final String operator;
    public final ExprNode right;

    public BinExprNode(ExprNode left, String operator, ExprNode right, int line, int col) {
        super(line, col);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBinExpr(this);
    }
}