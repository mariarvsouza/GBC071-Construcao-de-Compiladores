public final class TypeResolver {

    private TypeResolver() {
        //Classe utilitaria
    }

    public static Type resolver(String nome) {
        if (nome == null) {
            return Type.ERROR;
        }

        switch (nome) {
            case "number":
                return Type.NUMBER;

            case "real":
                return Type.REAL;

            case "bool":
                return Type.BOOL;

            case "char":
                return Type.CHAR;

            case "string":
                return Type.STRING;

            case "void":
                return Type.VOID;

            default:
                return Type.ERROR;
        }
    }
}
