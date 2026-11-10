import java.util.List;

public class Main {
    public static void main(String args[]) {
            String codigoFonte = "number process (number x) {\n" +
                "   if (x > 0) {\n" +
                "       for (number i=0; i<x; i=i+1) {\n" +
                "           x = x-1;\n" +
                "       }\n" +
                "   }\n" +
                "   return x;\n" +
                "}";

        System.out.println("Código fonte:");
        System.out.println(codigoFonte);

        Scanner scanner = new Scanner(codigoFonte);
        Parser parser = new Parser(scanner);
        Parser.ParseResult result = parser.parse();

        if (result.isSucesso()) {
            System.out.println("Nenhum erro encontrado.\n");

            VisitorImpressora impressoraAst = new VisitorImpressora();
            String astTexto = result.ast.accept(impressoraAst);
            System.out.println("Árvore AST:");
            System.out.println(astTexto);

            VisitorReimpressora reimpressoraCodigo = new VisitorReimpressora();
            String codigoReimpresso = result.ast.accept(reimpressoraCodigo);
            System.out.println("Código reimpresso:");
            System.out.println(codigoReimpresso);

        } else {
            System.out.println("Erros encontrados:");
            List<String> erros = result.erros;
            for (String erro : erros) {
                System.out.println(erro);
            }
        }
    }
}