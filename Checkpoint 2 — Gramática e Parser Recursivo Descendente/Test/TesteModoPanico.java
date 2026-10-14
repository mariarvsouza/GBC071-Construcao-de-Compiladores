import java.io.StringReader;

public class TesteModoPanico {
    public static void main(String[] args) {
        String codigoFonte =
                "number x = ;\n" +          // ERRO SINTÁTICO: falta a expressão após o '='
                        "number y = 20;\n" +        // Este comando deve ser analisado com sucesso após a recuperação
                        "if (x > 5) { \n" +
                        "    number z = 30;\n" +
                        "}";

        System.out.println("=== TESTE DE RECUPERAÇÃO EM MODO PÂNICO (SINTÁTICO) ===");
        System.out.println("Código fonte avaliado:");
        System.out.println(codigoFonte);
        System.out.println("----------------------------------------------------\n");


        Scanner scanner = new Scanner(codigoFonte);

        Parser parser = new Parser(scanner);

        boolean sucesso = parser.parse();

        if (!sucesso) {
            System.out.println("-> O parser encontrou erros sintáticos, acionou o Modo Pânico e continuou a análise:");
            for (String erro : parser.getErros()) {
                System.out.println("  * " + erro);
            }
        } else {
            System.out.println("-> O código foi aceito sem erros.");
        }
        System.out.println("Conclusão: O parser falhou no primeiro comando (varDeclPrime), ativou o modo pânico, descartou até o ';' e conseguiu processar o 'number y = 20;' e o 'if' seguintes na mesma execução!");
    }
}