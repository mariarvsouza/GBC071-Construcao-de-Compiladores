//Teste round-trip
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Teste4 {
    public static void main(String args[]) {
        String codigoFonte = "number calcular(number x, bool ativo) {\n" +
            "    if (!ativo) {\n" +
            "        return 0;\n" +
            "    }\n" +
            "    number total = x * 2;\n" +
            "    return total;\n" +
            "}";

        System.out.println("Código fonte:");
        System.out.println(codigoFonte);
        
        VisitorImpressora impressoraAst = new VisitorImpressora();
        VisitorReimpressora reimpressoraCodigo = new VisitorReimpressora();
        
        //Passagem 1:
        System.out.println("----Passagem 1:----");
        Scanner scanner1 = new Scanner(codigoFonte);
        Parser parser1 = new Parser(scanner1);
        Parser.ParseResult result1 = parser1.parse();
        
        if (!result1.isSucesso() || result1.ast == null) {
            System.out.println("Erros encontrados na passagem 1:");
            for (String erro : result1.erros) {
                System.out.println(erro);
            }
            return; 
        }

        System.out.println("Nenhum erro encontrado na passagem 1.\n");

        String astTexto1 = result1.ast.accept(impressoraAst).replace("\r\n", "\n").trim();
        System.out.println("Árvore AST gerada pelo Visitor (Passagem 1):");
        System.out.println(astTexto1);

        String codigoReimpresso1 = result1.ast.accept(reimpressoraCodigo);
        System.out.println("Código reimpresso gerado pelo Visitor (Passagem 1):");
        System.out.println(codigoReimpresso1);

        try{
            String astEsperada = Files.readString(Path.of("test/esperado/Teste4.txt")).replace("\r\n", "\n").trim();
            if (astEsperada.equals(astTexto1)) {
                System.out.println("Correto -> A AST gerada é igual à AST esperada (Passagem 1).");
            } else {
                System.out.println("Incorreto -> A AST gerada não é igual à AST esperada (Passagem 1).");
            }
        } catch(IOException e){
            System.out.println("Erro na leitura do arquivo");
        }

        
        //Passagem 2:
        System.out.println("\n\n----Passagem 2:----");
        Scanner scanner2 = new Scanner(codigoReimpresso1);
        Parser parser2 = new Parser(scanner2);
        Parser.ParseResult result2 = parser2.parse();
        
        if (!result2.isSucesso() || result2.ast == null) {
            System.out.println("Erros encontrados na passagem 2:");
            for (String erro : result2.erros) {
                System.out.println(erro);
            }
            return;
        }

        System.out.println("Nenhum erro encontrado na passagem 2.\n");

        String astTexto2 = result2.ast.accept(impressoraAst).replace("\r\n", "\n").trim();
        System.out.println("Árvore AST gerada pelo Visitor (Passagem 2):");
        System.out.println(astTexto2);

        String codigoReimpresso2 = result2.ast.accept(reimpressoraCodigo);
        System.out.println("Código reimpresso gerado pelo Visitor (Passagem 2):");
        System.out.println(codigoReimpresso2);

        boolean codigosIguais = codigoReimpresso1.equals(codigoReimpresso2);
        boolean arvoresIguais = astTexto1.equals(astTexto2);

        if (codigosIguais && arvoresIguais) {
            System.out.println("Teste de ida e volta correto");
        } else {
            System.out.println("Falha no teste de ida e volta");
            if (!codigosIguais) System.out.println("  - Código C1 difere de C2.");
            if (!arvoresIguais) System.out.println("  - Árvore AST1 difere de AST2.");
        }

    
    }
}