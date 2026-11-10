public class VisitorImpressora implements Visitor<String> {
    private int nivelIdentacao = 0;

    private String indent() {
        String indentacao = "";
        for (int i = 0; i<nivelIdentacao; i++) {
            indentacao += "  ";
        }
        return indentacao;
    }

    @Override
    public String visitProgram(ProgramNode node) {
        String resultado = "";
        resultado += indent()+"ProgramNode\n";
        nivelIdentacao++;
        for (StmtNode stmt : node.statements) {
            if (stmt != null) resultado += stmt.accept(this);
        }
        nivelIdentacao--;
        return resultado;
    }

    @Override
    public String visitFuncDecl(FuncDeclNode node) {
        String resultado = "";
        resultado += indent()+"FuncDeclNode(returnType: \""+node.returnType+ "\", name: \"" + node.name + "\")\n";
        nivelIdentacao++;
        for (ParamNode param : node.params) {
            if (param != null) resultado += param.accept(this);
        }
        if (node.body != null) {
            resultado += node.body.accept(this);
        }
        nivelIdentacao--;
        return resultado;
    }

    @Override
    public String visitParam(ParamNode node) {
        return indent()+"ParamNode(type: \""+node.type+"\", name: \""+node.name+"\")\n";
    }

    @Override
    public String visitBlock(BlockNode node) {
        String resultado = "";
        resultado += indent()+"BlockNode\n";
        nivelIdentacao++;
        for (StmtNode stmt : node.statements) {
            if (stmt != null) resultado += stmt.accept(this);
        }
        nivelIdentacao--;
        return resultado;
    }

    @Override
    public String visitVarDecl(VarDeclNode node) {
        String resultado = "";
        resultado += indent()+"VarDeclNode(type: \""+node.type+ "\", varID: \"" + node.varID + "\")\n";
        if (node.initExpr != null) {
            nivelIdentacao++;
            resultado += node.initExpr.accept(this);
            nivelIdentacao--;
        }
        return resultado;
    }

    @Override
    public String visitAssign(AssignNode node) {
        String resultado = "";
        resultado += indent()+"AssignNode(varID: \""+node.varID+"\")\n";
        nivelIdentacao++;
        if (node.expr != null) resultado += node.expr.accept(this);
        nivelIdentacao--;
        return resultado;
    }

    @Override
    public String visitIfStmt(IfStmtNode node) {
        String resultado = "";
        resultado += indent()+"IfStmtNode\n";
        nivelIdentacao++;
        if (node.condition != null) resultado += node.condition.accept(this);
        if (node.thenBlock != null) resultado += node.thenBlock.accept(this);
        if (node.elseBlock != null) resultado += node.elseBlock.accept(this);
        nivelIdentacao--;
        return resultado;
    }

    @Override
    public String visitForStmt(ForStmtNode node) {
        String resultado = "";
        resultado += indent()+"ForStmtNode\n";
        nivelIdentacao++;
        if (node.init != null) resultado += node.init.accept(this);
        if (node.condition != null) resultado += node.condition.accept(this);
        if (node.increment != null) resultado += node.increment.accept(this);
        if (node.body != null) resultado += node.body.accept(this);
        nivelIdentacao--;
        return resultado;
    }

    @Override
    public String visitReturnStmt(ReturnStmtNode node) {
        String resultado = "";
        resultado += indent()+"ReturnStmtNode\n";
        if (node.expr != null) {
            nivelIdentacao++;
            resultado += node.expr.accept(this);
            nivelIdentacao--;
        }
        return resultado;
    }

    @Override
    public String visitBreakStmt(BreakStmtNode node) {
        return indent()+"BreakStmtNode\n";
    }

    @Override
    public String visitCallFuncExpr(CallFuncExprNode node) {
        String resultado = "";
        resultado += indent()+"CallFuncExprNode(func: \""+node.func+"\")\n";
        nivelIdentacao++;
        for (ExprNode arg : node.args) {
            if (arg != null) resultado += arg.accept(this);
        }
        nivelIdentacao--;
        return resultado;
    }

    @Override
    public String visitUnaryExpr(UnaryExprNode node) {
        String resultado = "";
        resultado += indent()+"UnaryExprNode(operator: \""+node.operator+"\")\n";
        nivelIdentacao++;
        if (node.operand != null) resultado += node.operand.accept(this);
        nivelIdentacao--;
        return resultado;
    }

    @Override
    public String visitBinExpr(BinExprNode node) {
        String resultado = "";
        resultado += indent()+"BinExprNode(operator: \""+node.operator+"\")\n";
        nivelIdentacao++;
        if (node.left != null) resultado += node.left.accept(this);
        if (node.right != null) resultado += node.right.accept(this);
        nivelIdentacao--;
        return resultado;
    }

    @Override
    public String visitIdExpr(IdExprNode node) {
        return indent()+"IdExprNode(id: \""+node.id+"\")\n";
    }

    @Override
    public String visitIntLit(IntLitNode node) {
        return indent()+"IntLitNode(value: "+node.value+")\n";
    }

    @Override
    public String visitRealLit(RealLitNode node) {
        return indent()+"RealLitNode(value: "+node.value+")\n";
    }

    @Override
    public String visitCharLit(CharLitNode node) {
        return indent()+"CharLitNode(value: '"+node.value+"')\n";
    }

    @Override
    public String visitStringLit(StringLitNode node) {
        return indent()+"StringLitNode(value: \""+node.value+"\")\n";
    }

    @Override
    public String visitBoolLit(BoolLitNode node) {
        return indent()+"BoolLitNode(value: "+node.value+")\n";
    }
}