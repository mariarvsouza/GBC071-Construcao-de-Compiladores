import java.util.List;

public final class TesteProcedimentosERetornos {
    private static int executados;

    private static ProgramNode programa(StmtNode... nos) { return new ProgramNode(List.of(nos), 1, 1); }
    private static BlockNode bloco(StmtNode... nos) { return new BlockNode(List.of(nos), 1, 1); }
    private static FuncDeclNode funcao(String tipo, String nome, List<ParamNode> parametros, StmtNode... corpo) {
        return new FuncDeclNode(tipo, nome, parametros, bloco(corpo), 1, 1);
    }
    private static ParamNode param(String tipo, String nome) { return new ParamNode(tipo, nome, 1, 1); }
    private static CallFuncExprNode chamada(String nome, ExprNode... args) {
        return new CallFuncExprNode(nome, List.of(args), 3, 1);
    }
    private static IntLitNode inteiro(int n) { return new IntLitNode(n, 2, 1); }
    private static RealLitNode real(double n) { return new RealLitNode(n, 2, 1); }
    private static StringLitNode texto(String s) { return new StringLitNode(s, 2, 1); }
    private static BoolLitNode booleano(boolean b) { return new BoolLitNode(b, 2, 1); }
    private static ReturnStmtNode retorno(ExprNode e) { return new ReturnStmtNode(e, 2, 1); }
    private static VarDeclNode variavel(String tipo, String nome, ExprNode e) {
        return new VarDeclNode(tipo, nome, e, 3, 1);
    }
    private static IfStmtNode condicional(BlockNode sim, BlockNode nao) {
        return new IfStmtNode(booleano(true), sim, nao, 2, 1);
    }

    private static void conferir(String titulo, ProgramNode p, int numeroErros, String... trechos) {
        VisitorIdentificacao identificacao = new VisitorIdentificacao();
        p.accept(identificacao);
        if (!identificacao.getErros().isEmpty())
            throw new AssertionError(titulo + " - erro de identificacao: " + identificacao.getErros());
        new VisitorPreparacaoTipos(identificacao).preparar(p);
        VisitorVerificadorTipos tipos = new VisitorVerificadorTipos(identificacao);
        tipos.verificar(p);
        List<String> erros = tipos.getErros();
        if (erros.size() != numeroErros)
            throw new AssertionError(titulo + " - esperado " + numeroErros + " erro(s), obtido " + erros);
        for (String trecho : trechos) {
            boolean encontrado = false;
            for (String erro : erros) {
                if (erro.contains(trecho)) { encontrado = true; break; }
            }
            if (!encontrado)
                throw new AssertionError(titulo + " - nao encontrou '" + trecho + "' em " + erros);
        }
        if (!tipos.getAvisos().isEmpty())
            throw new AssertionError(titulo + " - aviso inesperado: " + tipos.getAvisos());
        executados++;
        System.out.println("[PASSOU] " + titulo + " (" + numeroErros + " erro(s))");
    }

    private static void tipoDaChamada(String titulo, ProgramNode p, CallFuncExprNode c,
                                     Type esperado, int numeroErros, String... trechos) {
        conferir(titulo, p, numeroErros, trechos);
        if (!esperado.equals(c.type))
            throw new AssertionError(titulo + " - tipo da chamada: esperado " + esperado + ", obtido " + c.type);
    }

    public static void main(String[] args) {
        // Procedimento sem retorno usado como comando: permitido
        CallFuncExprNode c1 = chamada("mostrar");
        tipoDaChamada("void como comando", programa(
                funcao("void", "mostrar", List.of(), new PrintStmtNode(inteiro(1), 2, 1)), c1),
                c1, Type.VOID, 0);

        // Procedimento sem retorno usado em expressao: proibido
        CallFuncExprNode c2 = chamada("mostrar");
        tipoDaChamada("void como inicializador", programa(
                funcao("void", "mostrar", List.of()), variavel("number", "x", c2)),
                c2, Type.ERROR, 1, "nao pode ser usado como expressao");

        CallFuncExprNode c3 = chamada("mostrar");
        tipoDaChamada("void dentro de print", programa(
                funcao("void", "mostrar", List.of()), new PrintStmtNode(c3, 3, 1)),
                c3, Type.ERROR, 1, "nao pode ser usado como expressao");

        CallFuncExprNode c4 = chamada("mostrar");
        tipoDaChamada("void como argumento", programa(
                funcao("void", "mostrar", List.of()),
                funcao("void", "receber", List.of(param("number", "a"))),
                chamada("receber", c4)),
                c4, Type.ERROR, 1, "nao pode ser usado como expressao");

        // Função com valor não pode ser chamada como comando puro
        CallFuncExprNode c5 = chamada("obter");
        tipoDaChamada("funcao com valor como comando", programa(
                funcao("number", "obter", List.of(), retorno(inteiro(1))), c5),
                c5, Type.ERROR, 1, "nao pode ser usada como comando");

        CallFuncExprNode c6 = chamada("obter");
        tipoDaChamada("funcao com valor como expressao", programa(
                funcao("number", "obter", List.of(), retorno(inteiro(1))), variavel("number", "x", c6)),
                c6, Type.NUMBER, 0);

        // Retornos válidos e inválidos
        conferir("return vazio em void", programa(funcao("void", "p", List.of(), retorno(null))), 0);
        conferir("return com valor em void", programa(funcao("void", "p", List.of(), retorno(inteiro(1)))),
                1, "retorno de 'p'");
        conferir("return vazio em number", programa(funcao("number", "f", List.of(), retorno(null))),
                1, "retorno de 'f'");
        conferir("number retornado em real", programa(funcao("real", "f", List.of(), retorno(inteiro(1)))), 0);
        conferir("real retornado em number", programa(funcao("number", "f", List.of(), retorno(real(1.5)))),
                1, "retorno de 'f'");
        conferir("tipo string retornado em bool", programa(funcao("bool", "f", List.of(), retorno(texto("a")))),
                1, "retorno de 'f'");
        conferir("funcao sem return", programa(funcao("number", "f", List.of(),
                new PrintStmtNode(inteiro(1), 2, 1))),
                1, "pode terminar sem retornar");
        conferir("void sem return explicito", programa(funcao("void", "p", List.of(),
                new PrintStmtNode(inteiro(1), 2, 1))), 0);
        conferir("if sem else: retorno nao garantido", programa(funcao("number", "f", List.of(),
                condicional(bloco(retorno(inteiro(1))), null))),
                1, "pode terminar sem retornar");
        conferir("if else: ambos retornam", programa(funcao("number", "f", List.of(),
                condicional(bloco(retorno(inteiro(1))), bloco(retorno(inteiro(2)))))), 0);
        conferir("if else: so um retorna", programa(funcao("number", "f", List.of(),
                condicional(bloco(retorno(inteiro(1))), bloco(new PrintStmtNode(inteiro(0), 2, 1))))),
                1, "pode terminar sem retornar");

        //Promoção, incompatibilidade e aridade
        CallFuncExprNode c18 = chamada("f", inteiro(1));
        tipoDaChamada("argumento number promovido a real", programa(
                funcao("real", "f", List.of(param("real", "a")), retorno(new IdExprNode("a", 2, 1))),
                variavel("real", "x", c18)), c18, Type.REAL, 0);

        CallFuncExprNode c19 = chamada("f", real(1.5));
        tipoDaChamada("argumento real rejeitado para number", programa(
                funcao("number", "f", List.of(param("number", "a")), retorno(new IdExprNode("a", 2, 1))),
                variavel("number", "x", c19)), c19, Type.ERROR, 1, "argumento 1");

        CallFuncExprNode c20 = chamada("f");
        tipoDaChamada("argumento ausente", programa(
                funcao("number", "f", List.of(param("number", "a")), retorno(new IdExprNode("a", 2, 1))),
                variavel("number", "x", c20)), c20, Type.ERROR, 1, "espera 1 argumento(s), recebeu 0");

        CallFuncExprNode c21 = chamada("f", inteiro(1), inteiro(2));
        tipoDaChamada("argumento extra", programa(
                funcao("number", "f", List.of(param("number", "a")), retorno(new IdExprNode("a", 2, 1))),
                variavel("number", "x", c21)), c21, Type.ERROR, 1, "espera 1 argumento(s), recebeu 2");

        System.out.println("PASSOU: " + executados + "/" + executados
                + " cenarios de procedimentos, funcoes e retornos");
    }
}
