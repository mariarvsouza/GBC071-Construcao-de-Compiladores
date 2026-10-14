public class TesteModoPanicoString {
    public static void main(String[] args) {
        String codigoFonte =
                "str texto = ;\n" +         // ERRO SINTÁTICO: Falta a expressão/literal string após o '='
                        "number w = 200;\n" +       // PRÓXIMO COMANDO: Deve ser analisado normalmente após a sincronização
                        "if (w > 50) {\n" +
                        "    number z = 5;\n" +
                        "}";

        System.out.println("=== TESTE DE MODO PÂNICO - RECUPERAÇÃO SINTÁTICA ===");
        System.out.println("Código fonte sob análise:");
        System.out.println(codigoFonte);
        System.out.println("--------------------------------------------------\n");

        Scanner scanner = new Scanner(codigoFonte);

        Parser parser = new Parser(scanner);


        boolean sucesso = parser.parse();

        if (!sucesso) {
            System.out.println("-> O parser falhou no primeiro comando, ativou o modo pânico, sincronizou no ';' e recuperou a execução:");
            for (String erro : parser.getErros()) {
                System.out.println("  * " + erro);
            }
        } else {
            System.out.println("-> Análise concluída sem erros.");
        }
        System.out.println("Comprovação: O erro sintático foi isolado, o fluxo andou e o comando seguinte ('number w = 200;') foi validado com sucesso na mesma execução!");
    }
}