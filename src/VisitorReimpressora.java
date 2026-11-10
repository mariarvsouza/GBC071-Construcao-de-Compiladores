public class VisitorReimpressora implements Visitor<String> {

    @Override
    public String visitProgram(ProgramNode node) {
        String resultado = "";
        for (StmtNode stmt : node.statements) {
            if (stmt != null) resultado += stmt.accept(this);
        }
        return resultado;
    }

    @Override
    public String visitFuncDecl(FuncDeclNode node) {
        String resultado = "";
        resultado += node.returnType+" "+node.name+"(";
        for (int i = 0; i<node.params.size(); i++) {
            resultado += node.params.get(i).accept(this);
            if (i<node.params.size()-1) resultado += ", ";
        }
        resultado += ") "+node.body.accept(this);
        return resultado;
    }

    @Override
    public String visitParam(ParamNode node) {
        return node.type+" "+node.name;
    }

    @Override
    public String visitBlock(BlockNode node) {
        String resultado = "";
        resultado += "{\n";
        for (StmtNode stmt : node.statements) {
            if (stmt != null) {
                resultado += "  "+stmt.accept(this).replace("\n", "\n  ");
                if (resultado.substring(resultado.length()-2).equals("  ")) {
                    resultado = resultado.substring(0, resultado.length()-2);
                }
            }
        }
        resultado += "}\n";
        return resultado;
    }

    @Override
    public String visitVarDecl(VarDeclNode node) {
        String resultado = "";
        resultado += node.type+" "+node.varID;
        if (node.initExpr != null) {
            resultado += " = "+node.initExpr.accept(this);
        }
        resultado += ";\n";
        return resultado;
    }

    @Override
    public String visitAssign(AssignNode node) {
        String expressao = "";
        if (node.expr != null) {
            expressao = node.expr.accept(this);
        }
        return node.varID+" = "+expressao+";\n";
    }

    @Override
    public String visitIfStmt(IfStmtNode node) {
        String resultado = "";
        String condicao = "";
        String blocoThen = "";
        if (node.condition != null) {
            condicao = node.condition.accept(this);
        }
        if (node.thenBlock != null) {
            blocoThen = node.thenBlock.accept(this);
        } else {
            blocoThen = "{} ";
        }
        resultado += "if ("+condicao+") ";
        resultado += blocoThen;
        if (node.elseBlock != null) {
            resultado += "else "+node.elseBlock.accept(this);
        }
        return resultado;
    }

    @Override
    public String visitForStmt(ForStmtNode node) {
        String resultado = "";
        String initStr = "";
        String incStr = "";
        String condStr = "";
        String corpo = "";
        if (node.init != null) {
            initStr = node.init.accept(this).trim();
        }
        if (initStr.endsWith(";")) {
            initStr = initStr.substring(0, initStr.length()-1);
        }
        if (node.increment != null) {
            incStr = node.increment.accept(this).trim();
        }
        if (incStr.endsWith(";")) {
            incStr = incStr.substring(0, incStr.length() - 1);
        }
        if (node.condition != null) {
            condStr = node.condition.accept(this);
        }
        if (node.body != null) {
            corpo = node.body.accept(this);
        } else {
            corpo = "{}\n";
        }
        resultado += "for ("+initStr+"; "+condStr+"; "+incStr+") ";
        resultado += corpo;
        return resultado;
    }

    @Override
    public String visitReturnStmt(ReturnStmtNode node) {
        if (node.expr != null) {
            return "return "+node.expr.accept(this)+";\n";
        }
        return "return;\n";
    }

    @Override
    public String visitBreakStmt(BreakStmtNode node) {
        return "break;\n";
    }

    @Override
    public String visitCallFuncExpr(CallFuncExprNode node) {
        String resultado = "";
        resultado += node.func + "(";
        for (int i = 0; i<node.args.size(); i++) {
            resultado += node.args.get(i).accept(this);
            if (i<node.args.size()-1) resultado += ", ";
        }
        resultado += ")";
        return resultado;
    }

    @Override
    public String visitUnaryExpr(UnaryExprNode node) {
        String operando = "";
        if (node.operand != null) {
            operando = node.operand.accept(this);
        }
        return node.operator+operando;
    }

    @Override
    public String visitBinExpr(BinExprNode node) {
        String leftStr = "";
        String rightStr = "";
        if (node.left != null) {
            leftStr = node.left.accept(this);
        }
        if (node.right != null) {
            rightStr = node.right.accept(this);
        }
        return "("+leftStr+" "+node.operator+" "+rightStr+")";
    }

    @Override
    public String visitIdExpr(IdExprNode node) {
        return node.id;
    }

    @Override
    public String visitIntLit(IntLitNode node) {
        return String.valueOf(node.value);
    }

    @Override
    public String visitRealLit(RealLitNode node) {
        return String.valueOf(node.value);
    }

    @Override
    public String visitCharLit(CharLitNode node) {
        return "'"+node.value+"'";
    }

    @Override
    public String visitStringLit(StringLitNode node) {
        return "\""+node.value+"\"";
    }

    @Override
    public String visitBoolLit(BoolLitNode node) {
        return String.valueOf(node.value);
    }
}