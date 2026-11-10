/** Testes isolados da tabela, sem depender do parser nem do Visitor. */
public class SymbolTableTest {
    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    public static void main(String[] args) {
        SymbolTable tabela = new StackSymbolTable();
        verificar(tabela.level() == 0, "Global deve ter nível 0");
        Symbol global = new Symbol("x", Kind.VAR, null, tabela.level());
        verificar(tabela.declare(global), "Declaração global deve funcionar");
        verificar(tabela.lookup("x") == global, "Busca global deve achar x");
        verificar(tabela.lookupLocal("x") == global, "Busca local global deve achar x");
        verificar(tabela.lookup("inexistente") == null, "Nome ausente deve devolver null");

        Symbol duplicado = new Symbol("x", Kind.FUNC, null, tabela.level());
        verificar(!tabela.declare(duplicado), "Redeclaração deve falhar");
        verificar(tabela.lookup("x") == global, "Primeira declaração deve permanecer");

        tabela.openScope();
        verificar(tabela.level() == 1, "Escopo interno deve ser nível 1");
        verificar(tabela.lookupLocal("x") == null, "lookupLocal não deve buscar fora");
        verificar(tabela.lookup("x") == global, "lookup deve buscar escopo externo");
        Symbol interno = new Symbol("x", Kind.VAR, null, tabela.level());
        verificar(tabela.declare(interno), "Sombreamento deve ser permitido");
        verificar(tabela.lookup("x") == interno, "Busca deve priorizar x interno");

        tabela.openScope();
        verificar(tabela.level() == 2, "Escopo aninhado deve ser nível 2");
        verificar(tabela.lookup("x") == interno, "Escopo aninhado deve ver x do pai");
        tabela.closeScope();
        verificar(tabela.lookup("x") == interno, "Após fechar filho, x interno persiste");
        tabela.closeScope();
        verificar(tabela.level() == 0, "Após fechar interno volta ao global");
        verificar(tabela.lookup("x") == global, "Após fechamento, x global é restaurado");

        boolean erroDocumentado = false;
        try {
            tabela.closeScope();
        } catch (IllegalStateException e) {
            erroDocumentado = true;
        }
        verificar(erroDocumentado, "Fechamento do global deve lançar exceção controlada");
        System.out.println("PASSOU: todos os testes isolados de SymbolTable");
    }
}
