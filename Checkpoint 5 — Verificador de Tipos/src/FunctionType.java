import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FunctionType extends Type {

    private final Type returnType;
    private final List<Type> paramTypes;

    public FunctionType(Type returnType, List<Type> paramTypes) {
        super("function");

        if (returnType == null || paramTypes == null) {
            throw new IllegalArgumentException(
                    "Assinatura de funcao incompleta"
            );
        }

        if (paramTypes.contains(null)) {
            throw new IllegalArgumentException(
                    "Parametro com tipo nulo"
            );
        }

        this.returnType = returnType;
        this.paramTypes = Collections.unmodifiableList(
                new ArrayList<>(paramTypes)
        );
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Type> getParamTypes() {
        return paramTypes;
    }

    public int getArity() {
        return paramTypes.size();
    }

    @Override
    public String toString() {
        return paramTypes + " -> " + returnType;
    }
}
