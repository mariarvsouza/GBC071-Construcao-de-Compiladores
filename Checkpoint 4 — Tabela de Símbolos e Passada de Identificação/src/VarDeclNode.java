import java.util.List;
import java.util.ArrayList;

public class VarDeclNode extends StmtNode {
    public final String type;
    public final String varID;
    public final ExprNode initExpr;

    public VarDeclNode(String type, String varID, ExprNode initExpr, int line, int col) {
        super(line, col);
        this.type = type;
        this.varID = varID;
        this.initExpr = initExpr;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVarDecl(this);
    }
}