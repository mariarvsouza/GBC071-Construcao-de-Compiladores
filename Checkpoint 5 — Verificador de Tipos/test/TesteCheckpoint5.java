import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class TesteCheckpoint5 {

    private static String normalizar(String texto) {
        String s = texto.replace("\r\n", "\n").replace('\r', '\n');
        while (s.endsWith("\n")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private static Path localizarPastaTestes() {
        Path[] opcoes = {
            Paths.get("test"), Paths.get("tests"), Paths.get("src", "test")
        };
        for (Path p : opcoes) {
            if (Files.isDirectory(p.resolve("programas")) &&
                Files.isDirectory(p.resolve("esperado"))) {
                return p;
            }
        }
        throw new IllegalStateException(
            "Não encontrei test/programas e test/esperado. " +
            "Configure o Working directory do IntelliJ para a raiz do projeto."
        );
    }

    private static String executar(Path arquivo) throws IOException {
        String codigoFonte = Files.readString(arquivo, StandardCharsets.UTF_8);
        Scanner scanner = new Scanner(codigoFonte);
        Parser parser = new Parser(scanner);
        Parser.ParseResult resultado = parser.parse();

        // Não executar visitantes sobre AST invalida
        if (!resultado.isSucesso()) {
            return String.join("\n", resultado.erros);
        }

        VisitorIdentificacao identificacao = new VisitorIdentificacao();
        resultado.ast.accept(identificacao);

        // Erros de nomes tem prioridade.
        // Preservamos sua mensagem sem tentar calcular tipos sobre declaracoes ausentes ou duplicadas
        if (!identificacao.getErros().isEmpty()) {
            return String.join("\n", identificacao.getErros());
        }

        VisitorPreparacaoTipos preparacao =
            new VisitorPreparacaoTipos(identificacao);
        preparacao.preparar(resultado.ast);

        VisitorVerificadorTipos verificador =
            new VisitorVerificadorTipos(identificacao);
        verificador.verificar(resultado.ast);

        List<String> diagnosticos = new ArrayList<>();
        diagnosticos.addAll(verificador.getErros());
        diagnosticos.addAll(verificador.getAvisos());
        return String.join("\n", diagnosticos);
    }

    public static void main(String[] args) throws IOException {
        Path pasta = localizarPastaTestes();
        Path programas = pasta.resolve("programas");
        Path esperados = pasta.resolve("esperado");
        List<Path> arquivos;
        try (Stream<Path> stream = Files.list(programas)) {
            arquivos = stream
                .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".txt"))
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .toList();
        }

        int aprovados = 0;
        int reprovados = 0;
        for (Path programa : arquivos) {
            String nome = programa.getFileName().toString();
            Path esperadoArquivo = esperados.resolve(nome);
            if (!Files.isRegularFile(esperadoArquivo)) {
                System.out.println("[FALHOU] " + nome + " (arquivo esperado ausente)");
                reprovados++;
                continue;
            }

            String esperado = normalizar(Files.readString(esperadoArquivo, StandardCharsets.UTF_8));
            try {
                String obtido = normalizar(executar(programa));
                if (esperado.equals(obtido)) {
                    System.out.println("[PASSOU] " + nome);
                    aprovados++;
                } else {
                    System.out.println("[FALHOU] " + nome);
                    System.out.println("  ESPERADO:\n" + (esperado.isEmpty() ? "  (vazio)" : esperado));
                    System.out.println("  OBTIDO:\n" + (obtido.isEmpty() ? "  (vazio)" : obtido));
                    reprovados++;
                }
            } catch (Exception e) {
                System.out.println("[FALHOU] " + nome + " (excecao: " + e + ")");
                reprovados++;
            }
        }
        System.out.println("\nRESULTADO: " + aprovados + "/" + arquivos.size() +
                           " passaram; " + reprovados + " falharam.");
        if (reprovados > 0) {
            throw new AssertionError("Existem testes divergentes; veja o relatorio acima.");
        }
    }
}
