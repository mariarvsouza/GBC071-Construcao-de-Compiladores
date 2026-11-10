import java.util.List;
import java.util.ArrayList;

public class ParamNode extends Node {
    public final String type;
    public final String name;

    public ParamNode(String type, String name, int line, int col) {
        super(line, col);
        this.type = type;
        this.name = name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitParam(this);
    }
}