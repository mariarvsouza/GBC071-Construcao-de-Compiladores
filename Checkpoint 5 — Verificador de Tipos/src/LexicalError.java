public class LexicalError extends RuntimeException {
    public LexicalError(String message, int line, int col) {
        super(String.format("Erro Léxico: %s na linha %d, coluna %d", message, line, col));
    }
}