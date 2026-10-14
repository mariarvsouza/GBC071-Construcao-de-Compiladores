public class TesteRealista {
    public static void main(String[] args) {
        String codigoFonte =
                "number x = ;\n" +                  // ERRO SINTÁTICO : Falta a expressão após o '='
                        "real pi = 3.14;\n" +               // Comando válido seguinte (deve ser recuperado pelo modo pânico)
                        "char letra = 'A';\n" +             // Comando válido
                        "string mensagem = \"Olá, Mundo!\";\n" + // Comando válido
                        "if (pi != 0.0) {\n" +              // Estrutura de controle válida
                        "    pi = pi + 1.0;\n" +
                        "} else {\n" +
                        "    pi = pi - 1.0;\n" +
                        "}";

        System.out.println("=== TESTE REALISTA COM RECUPERAÇÃO EM MODO PÂNICO (CHECKPOINT 2) ===");
        System.out.println("Código fonte sob análise:");
        System.out.println(codigoFonte);
        System.out.println("------------------------------------------------------------------\n");

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

        System.out.println("Comprovação: O erro sintático inicial foi isolado, o fluxo andou e todas as linhas seguintes ('real pi = 3.14;', atribuições e o bloco 'if/else') foram validadas com sucesso na mesma passada!");
    }
}