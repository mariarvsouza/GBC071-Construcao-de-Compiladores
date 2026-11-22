import java.util.List;
import java.util.ArrayList;

public class IdExprNode extends ExprNode {
    public final String id;

    // Vinculado pela passada de identificação (null se não declarado).
    public Symbol decl;

    public IdExprNode(String id, int line, int col) {
        super(line, col);
        this.id = id;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitIdExpr(this);
    }
}