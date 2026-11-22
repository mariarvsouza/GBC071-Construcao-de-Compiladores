import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class VisitorIdentificacao implements Visitor<Void> {
    private final SymbolTable tabela;
    private final List<String> erros = new ArrayList<>();
    private final Map<Node, Symbol> resolucoes = new IdentityHashMap<>();
    // Associa cada nó de declaração válida ao mesmo Symbol criado no CP4.
    private final Map<Node, Symbol> declaracoes = new IdentityHashMap<>();

    public VisitorIdentificacao() {
        this(new StackSymbolTable());
    }

    public VisitorIdentificacao(SymbolTable tabela) {
        if (tabela == null) {
            throw new IllegalArgumentException("Tabela de símbolos não pode ser nula");
        }
        this.tabela = tabela;
    }

    public List<String> getErros() {
        return Collections.unmodifiableList(erros);
    }

    public Symbol getResolucao(Node uso) {
        return resolucoes.get(uso);
    }

    public Symbol getSimboloDeclaracao(Node declaracao) {
        return declaracoes.get(declaracao);
    }

    private void visitar(Node node) {
        if (node != null) node.accept(this);
    }

    private void erro(Node node, String mensagem) {
        int linha = node == null ? -1 : node.line;
        erros.add("linha " + linha + ": " + mensagem);
    }

    private Symbol declarar(String nome, Kind categoria, Node declaracao) {
        if (nome == null || declaracao == null) return null;
        Symbol novo = new Symbol(nome, categoria, declaracao, tabela.level());
        if (!tabela.declare(novo)) {
            Symbol anterior = tabela.lookupLocal(nome);
            int linhaAnterior = anterior != null && anterior.decl != null ? anterior.decl.line : -1;
            erro(declaracao, "'" + nome + "' já declarado neste escopo (linha " + linhaAnterior + ")");
            return anterior; // mantém o primeiro na tabela; não substitui
        }
        declaracoes.put(declaracao, novo);
        return novo;
    }

    private Symbol usar(String nome, Node uso) {
        if (nome == null || uso == null) return null;
        Symbol s = tabela.lookup(nome);
        if (s == null) {
            erro(uso, "'" + nome + "' não declarado");
            return null;
        }
        return s;
    }

    @Override public Void visitPrintStmt(PrintStmtNode node) {
        visitar(node.expr);
        return null;
    }

    @Override public Void visitReadStmt(ReadStmtNode node) {
        Symbol destino = usar(node.varID, node);
        if (destino != null) resolucoes.put(node, destino);
        return null;
    }

    @Override public Void visitProgram(ProgramNode node) {
        for (StmtNode stmt : node.statements) visitar(stmt);
        return null;
    }

    @Override public Void visitFuncDecl(FuncDeclNode node) {
        // Declara no escopo externo antes do corpo: permite recursão direta.
        declarar(node.name, Kind.FUNC, node);
        tabela.openScope();
        try {
            for (ParamNode param : node.params) visitar(param);
            // Não chamar visitBlock aqui: parâmetros e corpo compartilham escopo.
            if (node.body != null) {
                for (StmtNode stmt : node.body.statements) visitar(stmt);
            }
        } finally {
            tabela.closeScope();
        }
        return null;
    }

    @Override public Void visitParam(ParamNode node) {
        declarar(node.name, Kind.PARAM, node);
        return null;
    }

    @Override public Void visitBlock(BlockNode node) {
        tabela.openScope();
        try {
            for (StmtNode stmt : node.statements) visitar(stmt);
        } finally {
            tabela.closeScope();
        }
        return null;
    }

    @Override
    public Void visitVarDecl(VarDeclNode node) {
        visitar(node.initExpr);
        declarar(node.varID, Kind.VAR, node);
        return null;
    }

    @Override public Void visitAssign(AssignNode node) {
        Symbol destino = usar(node.varID, node);
        if (destino != null) {
            resolucoes.put(node, destino);
        }
        visitar(node.expr);
        return null;
    }

    @Override public Void visitIfStmt(IfStmtNode node) {
        visitar(node.condition);
        visitar(node.thenBlock);
        visitar(node.elseBlock);
        return null;
    }

    @Override public Void visitForStmt(ForStmtNode node) {
        tabela.openScope();
        try {
            visitar(node.init);
            visitar(node.condition);
            visitar(node.increment);
            visitar(node.body); // abre MAIS UM escopo para o bloco do for
        } finally {
            tabela.closeScope();
        }
        return null;
    }

    @Override public Void visitReturnStmt(ReturnStmtNode node) {
        visitar(node.expr);
        return null;
    }

    @Override public Void visitBreakStmt(BreakStmtNode node) { return null; }

    @Override public Void visitCallFuncExpr(CallFuncExprNode node) {
        Symbol funcao = usar(node.func, node);
        if (funcao != null) {
            resolucoes.put(node, funcao);
        }
        for (ExprNode arg : node.args) visitar(arg);
        return null;
    }

    @Override public Void visitUnaryExpr(UnaryExprNode node) {
        visitar(node.operand);
        return null;
    }

    @Override public Void visitBinExpr(BinExprNode node) {
        visitar(node.left);
        visitar(node.right);
        return null;
    }

    @Override public Void visitIdExpr(IdExprNode node) {
        node.decl = usar(node.id, node);
        return null;
    }

    @Override public Void visitIntLit(IntLitNode node) { return null; }
    @Override public Void visitRealLit(RealLitNode node) { return null; }
    @Override public Void visitCharLit(CharLitNode node) { return null; }
    @Override public Void visitStringLit(StringLitNode node) { return null; }
    @Override public Void visitBoolLit(BoolLitNode node) { return null; }
}
