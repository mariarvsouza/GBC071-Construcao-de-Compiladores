import java.util.List;

public final class TesteInicializacao {
    private static int executados = 0;

    private static ProgramNode programa(StmtNode... comandos) {
        return new ProgramNode(List.of(comandos), 1, 1);
    }
    private static BlockNode bloco(int linha, StmtNode... comandos) {
        return new BlockNode(List.of(comandos), linha, 1);
    }
    private static VarDeclNode numero(String nome, ExprNode inicial, int linha) {
        return new VarDeclNode("number", nome, inicial, linha, 1);
    }
    private static IdExprNode id(String nome, int linha) {
        return new IdExprNode(nome, linha, 1);
    }
    private static IntLitNode inteiro(int valor, int linha) {
        return new IntLitNode(valor, linha, 1);
    }
    private static BoolLitNode booleano(boolean valor, int linha) {
        return new BoolLitNode(valor, linha, 1);
    }
    private static PrintStmtNode imprimir(ExprNode expr, int linha) {
        return new PrintStmtNode(expr, linha, 1);
    }
    private static AssignNode atribuir(String nome, ExprNode expr, int linha) {
        return new AssignNode(nome, expr, linha, 1);
    }
    private static void conferir(String titulo, ProgramNode programa, String... avisosEsperados) {
        VisitorIdentificacao identificacao = new VisitorIdentificacao();
        programa.accept(identificacao);
        if (!identificacao.getErros().isEmpty()) {
            throw new AssertionError(titulo + " - erros inesperados de identificação: "
                    + identificacao.getErros());
        }

        new VisitorPreparacaoTipos(identificacao).preparar(programa);
        VisitorVerificadorTipos verificador = new VisitorVerificadorTipos(identificacao);
        verificador.verificar(programa);
        if (!verificador.getErros().isEmpty()) {
            throw new AssertionError(titulo + " - erros inesperados de tipo: " + verificador.getErros());
        }
        List<String> esperado = List.of(avisosEsperados);
        if (!esperado.equals(verificador.getAvisos())) {
            throw new AssertionError(titulo + "\nESPERADO: " + esperado
                    + "\nOBTIDO: " + verificador.getAvisos());
        }
        executados++;
        System.out.println("[PASSOU] " + titulo + " (" + esperado.size() + " aviso(s))");
    }

    private static String aviso(String nome, int linha) {
        return "linha " + linha + ": aviso: variavel '" + nome
                + "' pode nao ter sido inicializada";
    }

    public static void main(String[] args) {
        // 1. Uso direto sem atribuir valor: deve avisar
        conferir("uso antes da inicialização",
                programa(numero("x", null, 1), imprimir(id("x", 2), 2)),
                aviso("x", 2));

        // 2. O inicializador define o valor
        conferir("declaração com inicializador",
                programa(numero("x", inteiro(10, 1), 1), imprimir(id("x", 2), 2)));

        // 3. Uma atribuição antes do uso define o valor
        conferir("atribuição anterior ao uso",
                programa(numero("x", null, 1), atribuir("x", inteiro(2, 2), 2),
                        imprimir(id("x", 3), 3)));

        // 4. read inicializa uma variavel legivel.
        conferir("read inicializa",
                programa(numero("x", null, 1), new ReadStmtNode("x", 2, 1),
                        imprimir(id("x", 3), 3)));

        // 5. Atribuir valor a outra variavel não inicializa a primeira
        conferir("variaveis independentes",
                programa(numero("x", null, 1), numero("y", inteiro(2, 2), 2),
                        imprimir(id("x", 3), 3), imprimir(id("y", 4), 4)),
                aviso("x", 3));

        // 6. Um unico ramo if não garante inicialização
        conferir("if sem else não garante inicialização",
                programa(numero("x", null, 1),
                        new IfStmtNode(booleano(true, 2),
                                bloco(2, atribuir("x", inteiro(3, 3), 3)), null, 2, 1),
                        imprimir(id("x", 5), 5)),
                aviso("x", 5));

        // 7. Se ambos os ramos atribuem, a variavel esta inicializada
        conferir("if e else inicializam",
                programa(numero("x", null, 1),
                        new IfStmtNode(booleano(true, 2),
                                bloco(2, atribuir("x", inteiro(3, 3), 3)),
                                bloco(4, atribuir("x", inteiro(4, 5), 5)), 2, 1),
                        imprimir(id("x", 7), 7)));

        // 8. Apenas um dos dois ramos atribui
        conferir("só um ramo do if inicializa",
                programa(numero("x", null, 1),
                        new IfStmtNode(booleano(false, 2),
                                bloco(2, atribuir("x", inteiro(3, 3), 3)),
                                bloco(4, imprimir(inteiro(0, 5), 5)), 2, 1),
                        imprimir(id("x", 7), 7)),
                aviso("x", 7));

        // 9. Corpo do for pode ser executado zero vezes
        conferir("for não garante inicializacao após o laço",
                programa(numero("x", null, 1),
                        new ForStmtNode(
                                numero("i", inteiro(0, 2), 2),
                                new BinExprNode(id("i", 2), "<", inteiro(3, 2), 2, 1),
                                atribuir("i", new BinExprNode(id("i", 2), "+", inteiro(1, 2), 2, 1), 2),
                                bloco(2, atribuir("x", inteiro(1, 3), 3)), 2, 1),
                        imprimir(id("x", 5), 5)),
                aviso("x", 5));

        // 10. Variavel da inicialização do for esta definida no proprio laço
        conferir("variavel de controle do for inicializada",
                programa(new ForStmtNode(
                        numero("i", inteiro(0, 1), 1),
                        new BinExprNode(id("i", 1), "<", inteiro(2, 1), 1, 1),
                        atribuir("i", new BinExprNode(id("i", 1), "+", inteiro(1, 1), 1, 1), 1),
                        bloco(1, imprimir(id("i", 2), 2)), 1, 1)));

        // 11. Parametros chegam inicializados na entrada da função
        ParamNode parametro = new ParamNode("number", "p", 1, 15);
        FuncDeclNode funcao = new FuncDeclNode("number", "f", List.of(parametro),
                bloco(1, imprimir(id("p", 2), 2), new ReturnStmtNode(id("p", 3), 3, 1)), 1, 1);
        conferir("parametros iniciam inicializados", programa(funcao));

        // 12. O valor externo continua distinto de uma variavel que o sombreia
        conferir("sombreamento: variavel interna distinta",
                programa(numero("x", inteiro(10, 1), 1),
                        bloco(2, numero("x", null, 3), imprimir(id("x", 4), 4)),
                        imprimir(id("x", 6), 6)),
                aviso("x", 4));

        System.out.println("PASSOU: " + executados + "/" + executados
                + " casos de inicializacao e avisos");
    }
}
