public class TesteRealista {
    public static void main(String args[]) {
        String codigoFonte =
                "--> Comentário de linha inicial\n" +
                        "number x = 42;\n" +
                        "real pi = 3.14;\n" +
                        "char letra = 'A';\n" +
                        "string mensagem = \"Olá, Mundo!\";\n" +
                        "-> Comentário de bloco válido <-\n" +
                        "if (x == 42 && pi != 0.0) {\n" +
                        "    x = x + 1;\n" +
                        "} else {\n" +
                        "    x = x - 1;\n" +
                        "}";

        System.out.println("Código fonte de teste (Abrangente):");
        System.out.println(codigoFonte);
        System.out.println("\n-----------------------------------\n");
        System.out.println("Tokens gerados:");

        Scanner scanner = new Scanner(codigoFonte);
        Token token;

        try {
            do {
                token = scanner.nextToken();
                System.out.println(token);
            } while (token.type != TokenType.EOF);
        } catch (LexicalError e) {
            System.out.println(e.getMessage());
        }
    }
}
