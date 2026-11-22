import java.util.Arrays;

public final class TestePreparacaoTipos {

    private static void exigir(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError("FALHOU: " + descricao);
        }
    }

    private static void tipoEsperado(VisitorIdentificacao identificacao,
                                     Node declaracao, Type esperado, String descricao) {
        Symbol simbolo = identificacao.getSimboloDeclaracao(declaracao);
        exigir(simbolo != null, "simbolo nao encontrado: " + descricao);
        exigir(esperado.equals(simbolo.type),
                descricao + " deveria ser " + esperado + ", mas foi " + simbolo.type);
    }

    public static void main(String[] args) {
        // number global = 10;
        VarDeclNode global = new VarDeclNode(
                "number", "global", new IntLitNode(10, 1, 17), 1, 1);

        ParamNode a = new ParamNode("number", "a", 2, 12);
        ParamNode b = new ParamNode("real", "b", 2, 22);
        VarDeclNode resultado = new VarDeclNode("real", "resultado", null, 3, 5);
        IdExprNode usoDeB = new IdExprNode("b", 4, 12);
        ReturnStmtNode retorno = new ReturnStmtNode(usoDeB, 4, 5);
        BlockNode corpo = new BlockNode(Arrays.asList(resultado, retorno), 2, 33);
        FuncDeclNode media = new FuncDeclNode(
                "real", "media", Arrays.asList(a, b), corpo, 2, 1);

        ProgramNode programa = new ProgramNode(Arrays.asList(global, media), 1, 1);

        VisitorIdentificacao identificacao = new VisitorIdentificacao();
        programa.accept(identificacao);
        exigir(identificacao.getErros().isEmpty(),
                "identificacao: " + identificacao.getErros());

        VisitorPreparacaoTipos preparacao =
                new VisitorPreparacaoTipos(identificacao);
        preparacao.preparar(programa);

        tipoEsperado(identificacao, global, Type.NUMBER, "variavel global");
        tipoEsperado(identificacao, a, Type.NUMBER, "parametro a");
        tipoEsperado(identificacao, b, Type.REAL, "parametro b");
        tipoEsperado(identificacao, resultado, Type.REAL, "variavel local");

        Symbol simboloFuncao = identificacao.getSimboloDeclaracao(media);
        exigir(simboloFuncao != null, "simbolo da funcao nao encontrado");
        exigir(simboloFuncao.type instanceof FunctionType,
                "a função deveria ter uma assinatura FunctionType");

        FunctionType assinatura = (FunctionType) simboloFuncao.type;
        exigir(Type.REAL.equals(assinatura.getReturnType()),
                "retorno da função deveria ser real");
        exigir(assinatura.getArity() == 2, "função deveria ter 2 parametros");
        exigir(Type.NUMBER.equals(assinatura.getParamTypes().get(0)),
                "primeiro parametro deveria ser number");
        exigir(Type.REAL.equals(assinatura.getParamTypes().get(1)),
                "segundo parametro deveria ser real");

        exigir(usoDeB.decl == identificacao.getSimboloDeclaracao(b),
                "uso de 'b' deve apontar para o mesmo simbolo do parametro");
        exigir(Type.REAL.equals(usoDeB.decl.type),
                "o uso de 'b' deve encontrar o tipo preparado no simbolo");

        System.out.println("PASSOU: tipos de variaveis, parametros, assinatura de funcao e vinculos CP4/CP5");
    }
}
