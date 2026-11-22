import java.util.List;

public final class TesteOperadoresAST {
    private static int total = 0;

    private static ExprNode literal(Type t, int linha) {
        if (Type.NUMBER.equals(t)) return new IntLitNode(2, linha, 1);
        if (Type.REAL.equals(t)) return new RealLitNode(2.5, linha, 1);
        if (Type.BOOL.equals(t)) return new BoolLitNode(true, linha, 1);
        if (Type.CHAR.equals(t)) return new CharLitNode('a', linha, 1);
        if (Type.STRING.equals(t)) return new StringLitNode("texto", linha, 1);
        throw new IllegalArgumentException("Tipo sem literal: " + t);
    }

    private static void conferir(String nome, ExprNode expr, Type esperado, String operador) {
        // Programa artificial: a variavel recebe a expressao em uma AST pronta
        // O tipo declarado coincide com o resultado esperado para casos validos, ou e number para casos invalidos
        // Assim evita erro independente de init
        String declarado = Type.ERROR.equals(esperado) ? "number" : esperado.toString();
        VarDeclNode variavel = new VarDeclNode(declarado, "resultado", expr, 1, 1);
        ProgramNode programa = new ProgramNode(List.of(variavel), 1, 1);

        VisitorIdentificacao identificacao = new VisitorIdentificacao();
        programa.accept(identificacao);
        if (!identificacao.getErros().isEmpty()) {
            throw new AssertionError(nome + " - identificacao: " + identificacao.getErros());
        }
        new VisitorPreparacaoTipos(identificacao).preparar(programa);
        VisitorVerificadorTipos verificador = new VisitorVerificadorTipos(identificacao);
        verificador.verificar(programa);

        if (expr.type == null || !esperado.equals(expr.type)) {
            throw new AssertionError(nome + " - tipo esperado " + esperado
                    + ", obtido " + expr.type + "; erros=" + verificador.getErros());
        }
        int esperadoErros = Type.ERROR.equals(esperado) ? 1 : 0;
        if (verificador.getErros().size() != esperadoErros) {
            throw new AssertionError(nome + " - esperados " + esperadoErros
                    + " erro(s), obtidos " + verificador.getErros());
        }
        if (esperadoErros == 1 && !verificador.getErros().get(0).contains("'" + operador + "'")) {
            throw new AssertionError(nome + " - erro nao menciona operador: " + verificador.getErros());
        }
        if (!verificador.getAvisos().isEmpty()) {
            throw new AssertionError(nome + " - avisos inesperados: " + verificador.getAvisos());
        }
        total++;
        System.out.println("[PASSOU] " + nome + " -> " + esperado);
    }

    private static void bin(String op, Type a, Type b, Type esperado) {
        String nome = a + " " + op + " " + b;
        conferir(nome, new BinExprNode(literal(a, 1), op, literal(b, 1), 1, 1), esperado, op);
    }

    private static void un(String op, Type a, Type esperado) {
        String nome = op + a;
        conferir(nome, new UnaryExprNode(op, literal(a, 1), 1, 1), esperado, op);
    }

    private static void semCascata() {
        BinExprNode interna = new BinExprNode(
                new BoolLitNode(true, 1, 1), "+", new IntLitNode(1, 1, 1), 1, 1);
        BinExprNode externa = new BinExprNode(interna, "*", new IntLitNode(2, 1, 1), 1, 1);
        VarDeclNode d = new VarDeclNode("number", "x", externa, 1, 1);
        ProgramNode programa = new ProgramNode(List.of(d), 1, 1);
        VisitorIdentificacao id = new VisitorIdentificacao();
        programa.accept(id);
        new VisitorPreparacaoTipos(id).preparar(programa);
        VisitorVerificadorTipos v = new VisitorVerificadorTipos(id);
        v.verificar(programa);
        if (!Type.ERROR.equals(interna.type) || !Type.ERROR.equals(externa.type)
                || v.getErros().size() != 1 || !v.getErros().get(0).contains("'+'")) {
            throw new AssertionError("Sem cascata: " + v.getErros());
        }
        total++;
        System.out.println("[PASSOU] propagacao de erro sem cascata");
    }

    public static void main(String[] args) {
        Type N = Type.NUMBER, R = Type.REAL, B = Type.BOOL;
        Type C = Type.CHAR, S = Type.STRING, E = Type.ERROR;

        // Cada operador aritmetico: tipos aceitos e recusados.
        for (String op : List.of("+", "-", "*", "/")) {
            bin(op, N, N, N);
            bin(op, N, R, R);
            bin(op, R, N, R);
            bin(op, R, R, R);
            bin(op, B, N, E);
            bin(op, C, N, E);
            bin(op, S, N, E);
        }
        // '+' tem uma regra adicional, especifica para strings.
        bin("+", S, S, S);
        bin("-", S, S, E);

        bin("%", N, N, N);
        bin("%", R, R, E);
        bin("%", N, R, E);
        bin("%", B, N, E);

        for (String op : List.of("<", "<=", ">", ">=")) {
            bin(op, N, N, B);
            bin(op, N, R, B);
            bin(op, R, N, B);
            bin(op, C, C, B);
            bin(op, S, S, E);
            bin(op, C, N, E);
            bin(op, B, B, E);
        }
        for (String op : List.of("==", "!=")) {
            for (Type t : List.of(N, R, B, C, S)) bin(op, t, t, B);
            bin(op, N, R, B);
            bin(op, R, N, B);
            bin(op, N, S, E);
            bin(op, B, N, E);
            bin(op, C, S, E);
        }
        for (String op : List.of("&&", "||")) {
            bin(op, B, B, B);
            bin(op, N, N, E);
            bin(op, B, N, E);
        }
        un("!", B, B);
        un("!", N, E);
        un("!", S, E);
        semCascata();
        System.out.println("PASSOU: " + total + "/" + total + " casos de operadores diretamente na AST");
    }
}
