public interface Visitor<T> {
    T visitProgram(ProgramNode node);
    T visitFuncDecl(FuncDeclNode node);
    T visitParam(ParamNode node);
    T visitBlock(BlockNode node);
    T visitVarDecl(VarDeclNode node);
    T visitAssign(AssignNode node);
    T visitIfStmt(IfStmtNode node);
    T visitForStmt(ForStmtNode node);
    T visitReturnStmt(ReturnStmtNode node);
    T visitBreakStmt(BreakStmtNode node);
    T visitCallFuncExpr(CallFuncExprNode node);
    T visitUnaryExpr(UnaryExprNode node);
    T visitBinExpr(BinExprNode node);
    T visitIdExpr(IdExprNode node);
    T visitIntLit(IntLitNode node);
    T visitRealLit(RealLitNode node);
    T visitCharLit(CharLitNode node);
    T visitStringLit(StringLitNode node);
    T visitBoolLit(BoolLitNode node);
}