import java.util.List;
import java.util.ArrayList;

public class FuncDeclNode extends StmtNode {
    public final String returnType;
    public final String name;
    public final List<ParamNode> params;
    public final BlockNode body;

    public FuncDeclNode(String returnType, String name, List<ParamNode> params, BlockNode body, int line, int col) {
        super(line, col);
        this.returnType = returnType;
        this.name = name;
        if (params != null) {
            this.params = params;
        } else {
            this.params = new ArrayList<>();
        }
        this.body = body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFuncDecl(this);
    }
}