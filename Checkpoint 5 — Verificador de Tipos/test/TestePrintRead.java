import java.util.List;

public final class TestePrintRead {
    private static void check(boolean ok, String mensagem) {
        if (!ok) throw new AssertionError(mensagem);
    }
    private static Parser.ParseResult analisar(String codigo) {
        return new Parser(new Scanner(codigo)).parse();
    }
    public static void main(String[] args) {
        String valido = "number x = 1;\nstring nome;\nprint(x + 2);\nread(nome);\n";
        Parser.ParseResult r = analisar(valido);
        check(r.isSucesso(), "programa valido rejeitado: " + r.erros);
        VisitorIdentificacao id = new VisitorIdentificacao();
        r.ast.accept(id);
        check(id.getErros().isEmpty(), "identificacao rejeitou programa: " + id.getErros());
        check(r.ast.statements.get(2) instanceof PrintStmtNode, "print não foi representado como no");
        check(r.ast.statements.get(3) instanceof ReadStmtNode, "read não foi representado como no");
        ReadStmtNode read = (ReadStmtNode) r.ast.statements.get(3);
        check(id.getResolucao(read) != null && id.getResolucao(read).decl.line == 2, "vinculo read incorreto");
        String reimpresso = r.ast.accept(new VisitorReimpressora());
        check(analisar(reimpresso).isSucesso(), "round-trip falhou: " + reimpresso);
        Parser.ParseResult invalido = analisar("read(10);\n");
        check(!invalido.isSucesso(), "read(10) deveria ser rejeitado sintaticamente");
        Parser.ParseResult ausente = analisar("read(faltante);\n");
        check(ausente.isSucesso(), "read(faltante) deveria ser sintaticamente valido");
        VisitorIdentificacao identificacao = new VisitorIdentificacao();
        ausente.ast.accept(identificacao);
        check(identificacao.getErros().stream().anyMatch(e -> e.contains("'faltante' não declarado")), "read de nome inexistente não detectado");
        check(analisar("print(4);\n").isSucesso(), "print literal deveria ser aceito");
        System.out.println("PASSOU: print/read, vinculos, erro sintatico e round-trip");
    }
}
