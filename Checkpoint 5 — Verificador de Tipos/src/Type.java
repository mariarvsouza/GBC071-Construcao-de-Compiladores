import java.util.Objects;

public class Type {

    public static final Type NUMBER = new Type("number");
    public static final Type REAL = new Type("real");
    public static final Type BOOL = new Type("bool");
    public static final Type CHAR = new Type("char");
    public static final Type STRING = new Type("string");
    public static final Type VOID = new Type("void");
    public static final Type ERROR = new Type("erro");

    private final String name;

    protected Type(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    public boolean isError() {
        return this.equals(ERROR);
    }

    public boolean isNumeric() {
        return this.equals(NUMBER) || this.equals(REAL);
    }

    public boolean isVoid() {
        return this.equals(VOID);
    }

    public boolean compatible(Type origem) {
        if (origem == null) {
            return false;
        }

        if (this.isError() || origem.isError()) {
            return true;
        }

        if (this.equals(origem)) {
            return true;
        }

        return this.equals(REAL) && origem.equals(NUMBER);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Type other = (Type) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
