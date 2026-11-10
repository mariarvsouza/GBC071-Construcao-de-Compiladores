import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Execucao automatizada com oraculos escritos manualmente em test/esperado. */
public final class Checkpoint4Suite {
    private static int executados;
    private static String normalizar(String s) { return s.replace("\r\n","\n").stripTrailing(); }
    private static void confirmar(boolean ok, String descricao) {
        if (!ok) throw new AssertionError(descricao);
    }
    private static String ler(String pasta, String nome) throws Exception {
        return Files.readString(Path.of("test",pasta,nome+".txt"));
    }
    private static void testar(String nome, boolean valido) throws Exception {
        String fonte = ler("programas",nome);
        Parser.ParseResult parsed = new Parser(new Scanner(fonte)).parse();
        confirmar(parsed.isSucesso() && parsed.ast!=null,
            nome+": erro sintatico inesperado: "+parsed.erros);
        VisitorIdentificacao identificador = new VisitorIdentificacao();
        parsed.ast.accept(identificador);
        StringBuilder obtido = new StringBuilder();
        for (String erro : identificador.getErros()) obtido.append(erro).append('\n');
        RelatorioVinculos relatorio = new RelatorioVinculos(identificador);
        parsed.ast.accept(relatorio);
        obtido.append(relatorio.texto());
        String esperado = normalizar(ler("esperado",nome));
        String real = normalizar(obtido.toString());
        confirmar(real.equals(esperado), nome+"\nESPERADO:\n"+esperado+"\nOBTIDO:\n"+real);
        confirmar(valido == identificador.getErros().isEmpty(), nome+": resultado da identificacao incorreto");
        executados++;
        System.out.println("OK " + nome);
    }
    private static void regressaoCp3() throws Exception {
        // Compara representacoes AST previamente escritas no CP3, sem gerar oraculos automaticamente.
        String[] fontes = {
          "void main() { number r1 = a - b - c; number r2 = a + b * c / d; }",
          "void main() { number x = (((a + b))); }",
          "number calcular(number x, bool ativo) { if (!ativo) { return 0; } number total = x * 2; return total; }"
        };
        String[] arquivos = { "Teste1.txt", "Teste2.txt", "Teste4.txt" };
        for (int i=0;i<fontes.length;i++) {
            Parser.ParseResult p = new Parser(new Scanner(fontes[i])).parse();
            confirmar(p.isSucesso(),"Regressao CP3: sintaxe invalida: " + p.erros);
            String arvore = normalizar(p.ast.accept(new VisitorImpressora()));
            String esperado = normalizar(Files.readString(Path.of("test","esperado_cp3",arquivos[i])));
            confirmar(arvore.equals(esperado),"Regressao CP3 falhou em "+arquivos[i]);
            String reimpresso = p.ast.accept(new VisitorReimpressora());
            Parser.ParseResult novamente = new Parser(new Scanner(reimpresso)).parse();
            confirmar(novamente.isSucesso(),"Round-trip CP3 nao analisavel: "+novamente.erros);
            confirmar(normalizar(novamente.ast.accept(new VisitorImpressora())).equals(arvore),
                "Round-trip CP3: arvores diferentes");
            confirmar(reimpresso.equals(novamente.ast.accept(new VisitorReimpressora())),
                "Round-trip CP3: codigo diferente");
            executados++;
            System.out.println("OK regressao CP3 / "+arquivos[i]);
        }
        String[] invalidos = {
           "number x = ;", "if x > 0 { number x = 1; }", "x = ;"
        };
        for (String fonte : invalidos) {
            Parser.ParseResult p = new Parser(new Scanner(fonte)).parse();
            confirmar(!p.isSucesso() && p.ast == null, "Regressao CP3: aceitou entrada invalida");
            executados++;
        }
        System.out.println("OK regressao CP3 / programas invalidos");
        Parser.ParseResult multi = new Parser(new Scanner("number x = ;\nnumber y = ;\n")).parse();
        confirmar(!multi.isSucesso() && multi.ast == null && multi.erros.size()>=2,
            "Regressao CP3: nao encontrou multiplos erros: "+multi.erros);
        executados++;
        System.out.println("OK regressao CP3 / multiplos erros");
    }
    public static void main(String[] args) throws Exception {
        // Tabela isolada permanece coberta pelo SymbolTableTest da Parte 2.
        // Tres programas principais: valido/sombreamento, redeclaracao e nao declarado.
        SymbolTableTest.main(new String[0]);
        executados++;
        testar("sombreamento_valido",true);
        testar("for_valido",true);
        testar("redeclaracao",false);
        testar("nome_nao_declarado",false);
        // Cada decisao de projeto recebe um teste proprio.
        testar("declarar_antes",false);
        testar("recursao_direta",true);
        testar("recursao_mutua",false);
        testar("parametro_corpo",false);
        testar("espaco_nomes",false);
        // Escopo do for e uso apos o fechamento; >=3 erros em mesma rodada.
        testar("for_escopos",false);
        testar("multiplos_erros",false);
        regressaoCp3();
        System.out.println("PASSOU: "+executados+" verificacoes automatizadas CP4 + regressao CP3");
    }
}
