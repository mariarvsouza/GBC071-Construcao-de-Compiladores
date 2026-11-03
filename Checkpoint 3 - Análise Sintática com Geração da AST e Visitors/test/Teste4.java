//Teste geral
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

        Scanner scanner = new Scanner(codigoFonte);
        Parser parser = new Parser(scanner);
        Parser.ParseResult result = parser.parse();

        if (result.isSucesso()) {
            System.out.println("Nenhum erro encontrado.\n");

            VisitorImpressora impressoraAst = new VisitorImpressora();
            String astTexto = result.ast.accept(impressoraAst).replace("\r\n", "\n").trim();
            System.out.println("Árvore AST gerada pelo Visitor:");
            System.out.println(astTexto);

            VisitorReimpressora reimpressoraCodigo = new VisitorReimpressora();
            String codigoReimpresso = result.ast.accept(reimpressoraCodigo);
            System.out.println("Código reimpresso gerado pelo Visitor:");
            System.out.println(codigoReimpresso);

            try{
                String astEsperada = Files.readString(Path.of(test/esperado/Teste4.txt)).replace("\r\n", "\n").trim();
                if (astEsperada.equals(astTexto)) {
                    System.out.println("\nCorreto -> A AST gerada é igual à AST esperada.");
                } else {
                    System.out.println("\nIncorreto -> A AST gerada não é igual à AST esperada.");
                }
            } catch(IOException e){
                System.out.println("Erro na leitura do arquivo");
            }

        } else {
            System.out.println("Erros encontrados:");
            List<String> erros = result.erros;
            for (String erro : erros) {
                System.out.println(erro);
            }
        }
    }
}
