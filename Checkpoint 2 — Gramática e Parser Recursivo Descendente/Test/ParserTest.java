import java.io.StringReader;

public class ParserTest{

    public static void main(String[] args){
        System.out.println("=== INICIANDO TESTES DO PARSER (CHECKPOINT 2) ===\n");

        //TESTES VÁLIDOS
        //------------------
        testarValido("Teste 1 (Declaração e Atribuição)", "number x = 10;");
        testarValido("Teste 2 (Estrutura If/Else)", "if (x > 0) { number y = 5; } else { number y = 0; }");
        testarValido("Teste 3 (Estrutura For)", "for (number i = 0; i < 10; i = i + 1) { break; }");

        //TESTES INVÁLIDOS
        //--------------------
        testarInvalido("Teste 4 (Falta ponto e vírgula)", "number x = 10");
        testarInvalido("Teste 5 (Parêntese faltando no if)", "if x > 0 { number x = 1; }");
        testarInvalido("Teste 6 (Atribuição inválida)", "x = ;");

        // 3. TESTE COM MÚLTIPLOS ERROS
        //--------------------------------
        testarMultiplosErros("Teste 7 (Modo Pânico com vários erros)",
                "number x = ;\n" + // Erro 1 na atribuição
                        "if x > 0 { number y = 2 }\n" + // Erro 2 faltando ponto e vírgula
                        "for (number i = 0 i < 5; i = i + 1) { }" // Erro 3 faltando ponto e vírgula no for
        );
    }

    private static void testarValido(String nome, String codigo){
        System.out.println("[VALIDO] " + nome);
        Scanner scanner = new Scanner(codigo);
        Parser parser = new Parser(scanner);
        boolean sucesso = parser.parse();

        if (sucesso){
            System.out.println(" -> PASSOU (Programa aceito corretamente).\n");
        }else{
            System.out.println(" -> FALHOU (Deveria ser válido, mas gerou erros):");
            for (String erro : parser.getErros()){
                System.out.println("    " + erro);
            }
            System.out.println();
        }
    }

    private static void testarInvalido(String nome, String codigo){
        System.out.println("[INVALIDO] " + nome);
        Scanner scanner = new Scanner(codigo);
        Parser parser = new Parser(scanner);
        boolean sucesso = parser.parse();

        if(!sucesso){
            System.out.println(" -> PASSOU (Erro detectado corretamente pelo parser):");
            for (String erro : parser.getErros()){
                System.out.println("    " + erro);
            }
            System.out.println();
        }else{
            System.out.println(" -> FALHOU (Deveria ter rejeitado o código, mas foi aceito).\n");
        }
    }

    private static void testarMultiplosErros(String nome, String codigo){
        System.out.println("[MULTIPLOS ERROS / MODO PÂNICO] " + nome);
        Scanner scanner = new Scanner(codigo);
        Parser parser = new Parser(scanner);
        boolean sucesso = parser.parse();

        System.out.println(" -> Análise concluída. O parser não travou e encontrou os seguintes erros:");
        for(String erro : parser.getErros()){
            System.out.println("    " + erro);
        }
        System.out.println("\n--------------------------------------------------\n");
    }
}
