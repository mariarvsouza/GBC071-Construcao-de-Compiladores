import java.util.List;

/** Testes de identificação; executa após CP3 válido. */
public class IdentificacaoTest {
    private static void confere(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    private static VisitorIdentificacao analisar(String fonte) {
        Parser.ParseResult sintaxe = new Parser(new Scanner(fonte)).parse();
        confere(sintaxe.isSucesso(), "Programa sintaticamente inválido: " + sintaxe.erros);
        VisitorIdentificacao identificador = new VisitorIdentificacao();
        sintaxe.ast.accept(identificador);
        return identificador;
    }

    public static void main(String[] args) {
        // Exemplo do desenho revisado: b é interno ao corpo do for.
        String foraDoBloco =
                "number g = 100;\n" +
                "number soma(number x) {\n" +
                "  number a = 10;\n" +
                "  for (number i = 0; i < x; i = i + 1) {\n" +
                "    number a = 20;\n" +
                "    number b = a + i + g;\n" +
                "  }\n" +
                "  b = 1;\n" +
                "  number c = a + g;\n" +
                "  return c;\n" +
                "}\n";
        VisitorIdentificacao ident = analisar(foraDoBloco);
        confere(ident.getErros().equals(List.of("linha 8: 'b' não declarado")),
                "Uso após fechamento: " + ident.getErros());

        // Verifica o vínculo real dos dois usos de x, não apenas ausência de erros.
        String sombra = "number x = 1;\n" +
                "{ number x = 2; number y = x; }\n" +
                "number z = x;\n";
        Parser.ParseResult resultadoSombra = new Parser(new Scanner(sombra)).parse();
        confere(resultadoSombra.isSucesso(), "Sombreamento deve ser sintaticamente válido");
        VisitorIdentificacao visitSombra = new VisitorIdentificacao();
        resultadoSombra.ast.accept(visitSombra);
        confere(visitSombra.getErros().isEmpty(), "Sombreamento não pode gerar erro");
        ProgramNode raiz = resultadoSombra.ast;
        BlockNode bloco = (BlockNode) raiz.statements.get(1);
        VarDeclNode local = (VarDeclNode) bloco.statements.get(0);
        VarDeclNode y = (VarDeclNode) bloco.statements.get(1);
        VarDeclNode global = (VarDeclNode) raiz.statements.get(0);
        VarDeclNode z = (VarDeclNode) raiz.statements.get(2);
        confere(((IdExprNode) y.initExpr).decl.decl == local, "Uso interno deve apontar para x local");
        confere(((IdExprNode) z.initExpr).decl.decl == global, "Uso externo deve apontar para x global");

        String recursaoDireta =
                "number f(number n) {\n" +
                "  if (n > 0) { return f(n - 1); }\n" +
                "  return 0;\n" +
                "}\n";
        confere(analisar(recursaoDireta).getErros().isEmpty(), "Recursão direta deve funcionar");

        String redeclaracao = "number x = 1;\nnumber x = 2;\n";
        confere(analisar(redeclaracao).getErros().equals(List.of(
                "linha 2: 'x' já declarado neste escopo (linha 1)")), "Redeclaração");

        String variosErros = "number a = 1;\nnumber a = 2;\nb = c;\n";
        confere(analisar(variosErros).getErros().equals(List.of(
                "linha 2: 'a' já declarado neste escopo (linha 1)",
                "linha 3: 'b' não declarado",
                "linha 3: 'c' não declarado")), "Múltiplos erros");

        String mutua = "number f() { return g(); }\nnumber g() { return f(); }\n";
        confere(analisar(mutua).getErros().equals(List.of("linha 1: 'g' não declarado")),
                "Recursão mútua adiantada deve ser rejeitada");

        System.out.println("PASSOU: testes básicos de identificação e escopo");
    }
}
