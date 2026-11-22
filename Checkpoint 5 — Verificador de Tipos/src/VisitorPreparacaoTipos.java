import java.util.ArrayList;
import java.util.List;

/**
 * CP5 - Prepara os tipos dos Symbol vinculados pela identificacao (CP4).
 *
 * Executar APOS VisitorIdentificacao. Nao declara nomes, nao abre/fecha
 * escopos e nao verifica expressoes. O verificador de tipos faz isso depois.
 */
public final class VisitorPreparacaoTipos implements Visitor<Void> {
    private final VisitorIdentificacao identificacao;

    public VisitorPreparacaoTipos(VisitorIdentificacao identificacao) {
        if (identificacao == null) {
            throw new IllegalArgumentException("Identificacao nao pode ser nula");
        }
        this.identificacao = identificacao;
    }

    public void preparar(ProgramNode programa) {
        if (programa != null) {
            programa.accept(this);
        }
    }

    private void visitar(Node node) {
        if (node != null) {
            node.accept(this);
        }
    }

    private void definirTipo(Node declaracao, Type tipo) {
        Symbol simbolo = identificacao.getSimboloDeclaracao(declaracao);
        if (simbolo != null) {
            simbolo.type = tipo;
        }
    }

    @Override
    public Void visitProgram(ProgramNode node) {
        for (StmtNode stmt : node.statements) visitar(stmt);
        return null;
    }

    @Override
    public Void visitFuncDecl(FuncDeclNode node) {
        List<Type> parametros = new ArrayList<>();
        for (ParamNode param : node.params) {
            parametros.add(TypeResolver.resolver(param.type));
        }
        Type retorno = TypeResolver.resolver(node.returnType);
        definirTipo(node, new FunctionType(retorno, parametros));

        for (ParamNode param : node.params) visitar(param);
        visitar(node.body);
        return null;
    }

    @Override
    public Void visitParam(ParamNode node) {
        definirTipo(node, TypeResolver.resolver(node.type));
        return null;
    }

    @Override
    public Void visitBlock(BlockNode node) {
        for (StmtNode stmt : node.statements) visitar(stmt);
        return null;
    }

    @Override
    public Void visitVarDecl(VarDeclNode node) {
        definirTipo(node, TypeResolver.resolver(node.type));
        // O inicializador sera conferido pelo VisitorVerificadorTipos.
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmtNode node) {
        visitar(node.thenBlock);
        visitar(node.elseBlock);
        return null;
    }

    @Override
    public Void visitForStmt(ForStmtNode node) {
        visitar(node.init);
        visitar(node.body);
        return null;
    }

    @Override public Void visitPrintStmt(PrintStmtNode node) { return null; }
    @Override public Void visitReadStmt(ReadStmtNode node) { return null; }
    @Override public Void visitAssign(AssignNode node) { return null; }
    @Override public Void visitReturnStmt(ReturnStmtNode node) { return null; }
    @Override public Void visitBreakStmt(BreakStmtNode node) { return null; }
    @Override public Void visitCallFuncExpr(CallFuncExprNode node) { return null; }
    @Override public Void visitUnaryExpr(UnaryExprNode node) { return null; }
    @Override public Void visitBinExpr(BinExprNode node) { return null; }
    @Override public Void visitIdExpr(IdExprNode node) { return null; }
    @Override public Void visitIntLit(IntLitNode node) { return null; }
    @Override public Void visitRealLit(RealLitNode node) { return null; }
    @Override public Void visitCharLit(CharLitNode node) { return null; }
    @Override public Void visitStringLit(StringLitNode node) { return null; }
    @Override public Void visitBoolLit(BoolLitNode node) { return null; }
}
