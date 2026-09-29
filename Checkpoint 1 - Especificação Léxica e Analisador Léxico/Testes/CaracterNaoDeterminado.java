public class CaracterNaoDeterminado {
    public class Main {
        public static void main(String args[]) {
            String codigoFonte =  "number x = 42 # 10;";
                //uso de caracteres não mapeados na linguagem
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
