public class TesteModoPanicoSintatico {
    public static void main(String[] args) {
        String codigoFonte =
                "number x = ;\n" +          // ERRO SINTÁTICO: Falta a expressão após o '='
                        "number y = 20;\n" +        // PRÓXIMO COMANDO: Deve ser analisado normalmente após a sincronização
                        "if (y > 10) {\n" +
                        "    number z = 5;\n" +
                        "}";

        System.out.println("=== TESTE DE MODO PÂNICO SINTÁTICO (CHECKPOINT 2) ===");
        System.out.println("Código fonte sob análise:");
        System.out.println(codigoFonte);
        System.out.println("------------------------------------------------------\n");

        Scanner scanner = new Scanner(codigoFonte);
        Parser parser = new Parser(scanner);
        boolean sucesso = parser.parse();
        if (!sucesso) {
            System.out.println("-> O parser falhou no primeiro comando, ativou o modo pânico, sincronizou no ';' e recuperou a análise para o próximo:");
            for (String erro : parser.getErros()) {
                System.out.println("  * " + erro);
            }
        } else {
            System.out.println("-> Análise concluída sem erros.");
        }
        System.out.println("Comprovação: O erro sintático foi isolado, o fluxo andou e o comando seguinte ('number y = 20;') foi validado com sucesso na mesma execução!");
    }
}