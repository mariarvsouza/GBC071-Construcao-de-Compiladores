public class Main {
    public static void main(String args[]) {
        String codigoFonte = "number x = 42\n" +
                             "real pi = 3.14\n" +
                             "if(x>42 || x==42) {\n" +
                             "    x = x+1\n" +
                             "}";
                             
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