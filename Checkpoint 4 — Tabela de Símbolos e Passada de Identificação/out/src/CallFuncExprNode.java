import java.util.List;
import java.util.ArrayList;

public class CallFuncExprNode extends ExprNode {
    public final String func;
    public final List<ExprNode> args;

    public CallFuncExprNode(String func, List<ExprNode> args, int line, int col) {
        super(line, col);
        this.func = func;
        if (args != null) {
            this.args = args;
        } else {
            this.args = new ArrayList<>();
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitCallFuncExpr(this);
    }
}