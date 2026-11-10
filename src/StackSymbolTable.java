import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementação de SymbolTable com uma pilha de mapas.
 * Existe sempre um escopo global (nível 0).
 */
public class StackSymbolTable implements SymbolTable {
    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();

    public StackSymbolTable() {
        scopes.push(new HashMap<>()); // escopo global, nível 0
    }

    @Override
    public void openScope() {
        scopes.push(new HashMap<>());
    }

    @Override
    public void closeScope() {
        if (scopes.size() == 1) {
            throw new IllegalStateException("Não é permitido fechar o escopo global");
        }
        scopes.pop();
    }

    @Override
    public boolean declare(Symbol s) {
        Objects.requireNonNull(s, "Símbolo não pode ser nulo");
        Objects.requireNonNull(s.name, "Nome do símbolo não pode ser nulo");
        Map<String, Symbol> atual = scopes.peek();
        if (atual.containsKey(s.name)) {
            return false; // não substitui a declaração original
        }
        atual.put(s.name, s);
        return true;
    }

    @Override
    public Symbol lookupLocal(String name) {
        return scopes.peek().get(name);
    }

    @Override
    public Symbol lookup(String name) {
        for (Map<String, Symbol> scope : scopes) {
            Symbol symbol = scope.get(name);
            if (symbol != null) {
                return symbol;
            }
        }
        return null;
    }

    @Override
    public int level() {
        return scopes.size() - 1;
    }
}
