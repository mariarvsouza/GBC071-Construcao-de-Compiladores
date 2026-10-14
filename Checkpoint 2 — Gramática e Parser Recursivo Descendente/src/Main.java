import java.util.List;

public class Main {
    public static void main(String args[]) {
        String codigoFonte = "number x = 42 * * 3;\n" +
                "real pi = 3.14;\n" +
                "if(x>42 {\n" +
                "    x = x + 1;\n" +
                "}";

        System.out.println("Código fonte:");
        System.out.println(codigoFonte);

        Scanner scanner = new Scanner(codigoFonte);
        Parser parser = new Parser(scanner);

        boolean sucesso = parser.parse();

        if (sucesso) {
            System.out.println("Análise concluída com sucesso. Nenhum erro encontrado.");
        } else {
            System.out.println("Análise concluída com falhas. Erros encontrados:");
            List<String> erros = parser.getErros();
            for (String erro : erros) {
                System.out.println(erro);
            }
        }
    }
}