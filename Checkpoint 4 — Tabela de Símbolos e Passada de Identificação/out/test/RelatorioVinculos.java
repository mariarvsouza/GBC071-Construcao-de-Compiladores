/** Relatorio deterministico de usos de nomes para comparacao com arquivos manuais.
 * Somente leitura da AST ja decorada pelo VisitorIdentificacao.
 */
public final class RelatorioVinculos implements Visitor<Void> {
    private final VisitorIdentificacao identificacao;
    private final StringBuilder saida = new StringBuilder();
    public RelatorioVinculos(VisitorIdentificacao identificacao) { this.identificacao = identificacao; }
    public String texto() { return saida.toString(); }
    private void descer(Node n) { if (n != null) n.accept(this); }
    private void uso(String nome, Node no, Symbol simbolo) {
        if (simbolo != null) {
            saida.append("linha ").append(no.line).append(": '").append(nome)
                 .append("' -> ").append(simbolo.kind).append("@")
                 .append(simbolo.decl.line).append("\n");
        }
    }
    @Override public Void visitProgram(ProgramNode n) { for (StmtNode s : n.statements) descer(s); return null; }
    @Override public Void visitFuncDecl(FuncDeclNode n) { for (ParamNode p : n.params) descer(p); descer(n.body); return null; }
    @Override public Void visitParam(ParamNode n) { return null; }
    @Override public Void visitBlock(BlockNode n) { for (StmtNode s : n.statements) descer(s); return null; }
    @Override public Void visitVarDecl(VarDeclNode n) { descer(n.initExpr); return null; }
    @Override public Void visitAssign(AssignNode n) { uso(n.varID,n,identificacao.getResolucao(n)); descer(n.expr); return null; }
    @Override public Void visitIfStmt(IfStmtNode n) { descer(n.condition); descer(n.thenBlock); descer(n.elseBlock); return null; }
    @Override public Void visitForStmt(ForStmtNode n) { descer(n.init); descer(n.condition); descer(n.increment); descer(n.body); return null; }
    @Override public Void visitReturnStmt(ReturnStmtNode n) { descer(n.expr); return null; }
    @Override public Void visitBreakStmt(BreakStmtNode n) { return null; }
    @Override public Void visitCallFuncExpr(CallFuncExprNode n) { uso(n.func,n,identificacao.getResolucao(n)); for (ExprNode a:n.args) descer(a); return null; }
    @Override public Void visitUnaryExpr(UnaryExprNode n) { descer(n.operand); return null; }
    @Override public Void visitBinExpr(BinExprNode n) { descer(n.left); descer(n.right); return null; }
    @Override public Void visitIdExpr(IdExprNode n) { uso(n.id,n,n.decl); return null; }
    @Override public Void visitIntLit(IntLitNode n) { return null; }
    @Override public Void visitRealLit(RealLitNode n) { return null; }
    @Override public Void visitCharLit(CharLitNode n) { return null; }
    @Override public Void visitStringLit(StringLitNode n) { return null; }
    @Override public Void visitBoolLit(BoolLitNode n) { return null; }
}
