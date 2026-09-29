public class StringNaoTerminada {
    public class Main {
        public static void main(String args[]) {
            String codigoFonte = "string texto = \"Texto sem fim;";
            //Falta a aspa dupla de fechamento
            System.out.println("Código fonte:");
            System.out.println(codigoFonte);
            System.out.println("");

            System.out.println("Tokens:");

            Scanner scanner = new Scanner(codigoFonte);
            Token token;

            try {
                do {
                    token = scanner.nextToken();
                    System.out.println(token);
                } while (token.type!=TokenType.EOF);
            } catch (LexicalError e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
