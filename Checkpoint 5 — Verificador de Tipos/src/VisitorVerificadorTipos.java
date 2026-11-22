import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * CP5 - Verificacao de tipos sobre AST ja identificada e com Symbol.type preparado.
 * Nao abre escopos, nao declara simbolos e nao inspeciona classes de nos com instanceof.
 * Regras: number -> real; demais conversoes implicitas proibidas.
 */
public final class VisitorVerificadorTipos implements Visitor<Void> {
    private final VisitorIdentificacao identificacao;
    private final List<String> erros = new ArrayList<>();
    private final List<String> avisos = new ArrayList<>();
    private final Map<Symbol, Boolean> inicializados = new IdentityHashMap<>();
    private final Map<Node, Boolean> retornaSempre = new IdentityHashMap<>();
    private FunctionType funcaoAtual;
    private String nomeFuncaoAtual;
    private int lacosAbertos;
    private boolean contextoComando;

    public VisitorVerificadorTipos(VisitorIdentificacao identificacao) {
        if (identificacao == null) throw new IllegalArgumentException("Identificacao nula");
        this.identificacao = identificacao;
    }

    /** Executar apos VisitorPreparacaoTipos.preparar(programa). */
    public void verificar(ProgramNode programa) {
        erros.clear();
        avisos.clear();
        inicializados.clear();
        retornaSempre.clear();
        funcaoAtual = null;
        nomeFuncaoAtual = null;
        lacosAbertos = 0;
        contextoComando = false;
        if (programa != null) programa.accept(this);
    }

    public List<String> getErros() { return Collections.unmodifiableList(erros); }
    public List<String> getAvisos() { return Collections.unmodifiableList(avisos); }
    public boolean temErros() { return !erros.isEmpty(); }

    private void erro(Node n, String msg) {
        erros.add("linha " + (n == null ? -1 : n.line) + ": " + msg);
    }
    private void aviso(Node n, String msg) {
        avisos.add("linha " + (n == null ? -1 : n.line) + ": aviso: " + msg);
    }
    private Type tipo(Type t) { return t == null ? Type.ERROR : t; }
    private Type simboloTipo(Symbol s) { return s == null ? Type.ERROR : tipo(s.type); }
    private boolean erroTipo(Type t) { return tipo(t).isError(); }
    private boolean atribuivel(Type destino, Type origem) {
        return tipo(destino).compatible(tipo(origem));
    }
    private boolean eh(Type t, Type outro) { return tipo(t).equals(outro); }
    private boolean numerico(Type t) { return tipo(t).isNumeric(); }
    private boolean primitivo(Type t) {
        return eh(t, Type.NUMBER) || eh(t, Type.REAL) || eh(t, Type.BOOL)
                || eh(t, Type.CHAR) || eh(t, Type.STRING);
    }
    private boolean legivel(Type t) {
        return eh(t, Type.NUMBER) || eh(t, Type.REAL)
                || eh(t, Type.CHAR) || eh(t, Type.STRING);
    }
    private void marcaRetorno(Node n, boolean valor) { if (n != null) retornaSempre.put(n, valor); }
    private boolean retorna(Node n) { return n != null && Boolean.TRUE.equals(retornaSempre.get(n)); }
    private boolean inicializado(Symbol s) {
        return s != null && Boolean.TRUE.equals(inicializados.get(s));
    }
    private Map<Symbol, Boolean> fotografia() { return new IdentityHashMap<>(inicializados); }
    private void restaurar(Map<Symbol, Boolean> estado) {
        inicializados.clear();
        inicializados.putAll(estado);
    }
    private void visitarComando(StmtNode n) {
        if (n == null) return;
        boolean anterior = contextoComando;
        contextoComando = true;
        n.accept(this);
        contextoComando = anterior;
    }
    private Type visitarExpr(ExprNode n) {
        if (n == null) return Type.ERROR;
        boolean anterior = contextoComando;
        contextoComando = false;
        n.accept(this);
        contextoComando = anterior;
        n.type = tipo(n.type);
        return n.type;
    }
    private void visitarBloco(BlockNode n) {
        if (n != null) n.accept(this);
    }

    @Override public Void visitProgram(ProgramNode n) {
        for (StmtNode stmt : n.statements) visitarComando(stmt);
        return null;
    }

    @Override public Void visitFuncDecl(FuncDeclNode n) {
        FunctionType anteriorFuncao = funcaoAtual;
        String anteriorNome = nomeFuncaoAtual;
        int anterioresLacos = lacosAbertos;
        Map<Symbol, Boolean> estadoAnterior = fotografia();
        Symbol s = identificacao.getSimboloDeclaracao(n);
        funcaoAtual = s != null && s.type instanceof FunctionType ? (FunctionType) s.type : null;
        nomeFuncaoAtual = n.name;
        lacosAbertos = 0;
        try {
            for (ParamNode p : n.params) visitParam(p);
            // O corpo principal compartilha o escopo dos parametros, mas isto
            // nao afeta a checagem: os vinculos ja foram determinados no CP4.
            visitarBloco(n.body);
            if (funcaoAtual != null && !funcaoAtual.getReturnType().isVoid()
                    && !erroTipo(funcaoAtual.getReturnType()) && !retorna(n.body)) {
                erro(n, "funcao '" + n.name + "' pode terminar sem retornar "
                        + funcaoAtual.getReturnType());
            }
        } finally {
            restaurar(estadoAnterior);
            funcaoAtual = anteriorFuncao;
            nomeFuncaoAtual = anteriorNome;
            lacosAbertos = anterioresLacos;
        }
        return null;
    }

    @Override public Void visitParam(ParamNode n) {
        Symbol s = identificacao.getSimboloDeclaracao(n);
        if (s != null) {
            inicializados.put(s, true);
            if (simboloTipo(s).isVoid()) erro(n, "parametro '" + n.name + "' nao pode ser void");
        }
        return null;
    }

    @Override public Void visitBlock(BlockNode n) {
        boolean garantido = false;
        for (StmtNode stmt : n.statements) {
            visitarComando(stmt);
            if (retorna(stmt)) garantido = true;
        }
        marcaRetorno(n, garantido);
        return null;
    }

    @Override public Void visitVarDecl(VarDeclNode n) {
        Symbol s = identificacao.getSimboloDeclaracao(n);
        Type declarado = s == null ? TypeResolver.resolver(n.type) : simboloTipo(s);
        if (eh(declarado, Type.VOID)) erro(n, "variavel '" + n.varID + "' nao pode ser void");
        if (s != null) inicializados.put(s, false);
        // Nao considera a variavel inicializada durante a propria expressao inicial.
        if (n.initExpr != null) {
            Type origem = visitarExpr(n.initExpr);
            if (!erroTipo(declarado) && !erroTipo(origem) && !atribuivel(declarado, origem)) {
                erro(n, "inicializacao de '" + n.varID + "': esperado " + declarado + ", veio " + origem);
            }
            if (s != null && !erroTipo(origem) && atribuivel(declarado, origem)) inicializados.put(s, true);
        }
        marcaRetorno(n, false);
        return null;
    }

    @Override public Void visitAssign(AssignNode n) {
        Symbol s = identificacao.getResolucao(n);
        Type origem = visitarExpr(n.expr);
        if (s != null) {
            if (s.kind != Kind.VAR && s.kind != Kind.PARAM) {
                erro(n, "'" + n.varID + "' nao e variavel nem parametro");
            } else {
                Type destino = simboloTipo(s);
                if (!erroTipo(origem) && !erroTipo(destino) && !atribuivel(destino, origem)) {
                    erro(n, "atribuicao a '" + n.varID + "': esperado " + destino + ", veio " + origem);
                } else if (!erroTipo(origem)) inicializados.put(s, true);
            }
        }
        marcaRetorno(n, false);
        return null;
    }

    @Override public Void visitIfStmt(IfStmtNode n) {
        Type cond = visitarExpr(n.condition);
        if (!erroTipo(cond) && !eh(cond, Type.BOOL)) {
            erro(n, "condicao de 'if' deve ser bool, mas e " + cond);
        }
        Map<Symbol, Boolean> antes = fotografia();
        visitarBloco(n.thenBlock);
        Map<Symbol, Boolean> depoisThen = fotografia();
        restaurar(antes);
        visitarBloco(n.elseBlock);
        Map<Symbol, Boolean> depoisElse = fotografia();
        restaurar(antes);
        // So garante inicializacao apos o if quando ambos os caminhos a garantem.
        for (Symbol s : antes.keySet()) {
            inicializados.put(s, Boolean.TRUE.equals(depoisThen.get(s))
                    && Boolean.TRUE.equals(depoisElse.get(s)));
        }
        marcaRetorno(n, n.elseBlock != null && retorna(n.thenBlock) && retorna(n.elseBlock));
        return null;
    }

    @Override public Void visitForStmt(ForStmtNode n) {
        visitarComando(n.init);
        Type cond = visitarExpr(n.condition);
        if (!erroTipo(cond) && !eh(cond, Type.BOOL)) {
            erro(n, "condicao de 'for' deve ser bool, mas e " + cond);
        }
        Map<Symbol, Boolean> antesIteracao = fotografia();
        lacosAbertos++;
        try {
            visitarBloco(n.body);
            visitarComando(n.increment);
        } finally {
            lacosAbertos--;
            // Corpo e incremento podem nao executar; manter apenas estado apos init.
            restaurar(antesIteracao);
        }
        marcaRetorno(n, false); // convencao conservadora do grupo
        return null;
    }

    @Override public Void visitReturnStmt(ReturnStmtNode n) {
        Type dado = n.expr == null ? Type.VOID : visitarExpr(n.expr);
        if (funcaoAtual == null) {
            erro(n, "'return' fora de funcao");
        } else {
            Type esperado = tipo(funcaoAtual.getReturnType());
            if (!erroTipo(dado) && !erroTipo(esperado) && !atribuivel(esperado, dado)) {
                erro(n, "retorno de '" + nomeFuncaoAtual + "': esperado " + esperado + ", veio " + dado);
            }
        }
        marcaRetorno(n, true);
        return null;
    }

    @Override public Void visitBreakStmt(BreakStmtNode n) {
        if (lacosAbertos <= 0) erro(n, "'break' fora de laco");
        marcaRetorno(n, false);
        return null;
    }

    @Override public Void visitPrintStmt(PrintStmtNode n) {
        Type t = visitarExpr(n.expr);
        if (!erroTipo(t) && !primitivo(t)) {
            erro(n, "'print' exige tipo primitivo nao void, veio " + t);
        }
        marcaRetorno(n, false);
        return null;
    }

    @Override public Void visitReadStmt(ReadStmtNode n) {
        Symbol s = identificacao.getResolucao(n);
        if (s != null) {
            Type t = simboloTipo(s);
            if (s.kind != Kind.VAR && s.kind != Kind.PARAM) {
                erro(n, "'read' exige variavel ou parametro: '" + n.varID + "'");
            } else if (!erroTipo(t) && !legivel(t)) {
                erro(n, "'read' nao aceita variavel '" + n.varID + "' do tipo " + t);
            } else if (!erroTipo(t)) {
                inicializados.put(s, true);
            }
        }
        marcaRetorno(n, false);
        return null;
    }

    @Override public Void visitCallFuncExpr(CallFuncExprNode n) {
        boolean chamadaComando = contextoComando;
        List<Type> argumentos = new ArrayList<>();
        for (ExprNode arg : n.args) argumentos.add(visitarExpr(arg));
        Symbol s = identificacao.getResolucao(n);
        if (s == null) { n.type = Type.ERROR; return null; } // CP4 ja reportou
        if (s.kind != Kind.FUNC || !(s.type instanceof FunctionType)) {
            erro(n, "'" + n.func + "' nao e funcao");
            n.type = Type.ERROR;
            return null;
        }
        FunctionType assinatura = (FunctionType) s.type;
        List<Type> parametros = assinatura.getParamTypes();
        boolean invalida = false;
        if (argumentos.size() != parametros.size()) {
            erro(n, "'" + n.func + "' espera " + parametros.size()
                    + " argumento(s), recebeu " + argumentos.size());
            invalida = true;
        }
        for (int i = 0; i < Math.min(argumentos.size(), parametros.size()); i++) {
            Type esperado = tipo(parametros.get(i));
            Type recebido = tipo(argumentos.get(i));
            if (erroTipo(recebido) || erroTipo(esperado)) {
                invalida = true;
            } else if (!atribuivel(esperado, recebido)) {
                erro(n, "argumento " + (i + 1) + " de '" + n.func
                        + "': esperado " + esperado + ", veio " + recebido);
                invalida = true;
            }
        }
        Type retorno = tipo(assinatura.getReturnType());
        if (chamadaComando && !retorno.isVoid() && !erroTipo(retorno)) {
            erro(n, "funcao '" + n.func + "' retorna " + retorno + " e nao pode ser usada como comando");
            invalida = true;
        } else if (!chamadaComando && retorno.isVoid()) {
            erro(n, "procedimento '" + n.func + "' nao pode ser usado como expressao");
            invalida = true;
        }
        n.type = invalida ? Type.ERROR : retorno;
        marcaRetorno(n, false);
        return null;
    }

    @Override public Void visitUnaryExpr(UnaryExprNode n) {
        Type operando = visitarExpr(n.operand);
        if (erroTipo(operando)) { n.type = Type.ERROR; return null; }
        if ("!".equals(n.operator) && eh(operando, Type.BOOL)) {
            n.type = Type.BOOL;
        } else {
            erro(n, "operador unario '" + n.operator + "' incompativel com " + operando);
            n.type = Type.ERROR;
        }
        return null;
    }

    @Override public Void visitBinExpr(BinExprNode n) {
        Type a = visitarExpr(n.left);
        Type b = visitarExpr(n.right);
        if (erroTipo(a) || erroTipo(b)) { n.type = Type.ERROR; return null; }
        String op = n.operator;
        Type resultado = null;
        if ("+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op)) {
            if ("+".equals(op) && eh(a, Type.STRING) && eh(b, Type.STRING)) {
                resultado = Type.STRING;
            } else if (numerico(a) && numerico(b)) {
                resultado = eh(a, Type.REAL) || eh(b, Type.REAL) ? Type.REAL : Type.NUMBER;
            }
        } else if ("%".equals(op)) {
            if (eh(a, Type.NUMBER) && eh(b, Type.NUMBER)) resultado = Type.NUMBER;
        } else if ("<".equals(op) || "<=".equals(op) || ">".equals(op) || ">=".equals(op)) {
            if ((numerico(a) && numerico(b)) || (eh(a, Type.CHAR) && eh(b, Type.CHAR))) resultado = Type.BOOL;
        } else if ("==".equals(op) || "!=".equals(op)) {
            if ((primitivo(a) && a.equals(b)) || (numerico(a) && numerico(b))) resultado = Type.BOOL;
        } else if ("&&".equals(op) || "||".equals(op)) {
            if (eh(a, Type.BOOL) && eh(b, Type.BOOL)) resultado = Type.BOOL;
        }
        if (resultado == null) {
            erro(n, "operador '" + op + "' incompativel com " + a + " e " + b);
            n.type = Type.ERROR;
        } else {
            n.type = resultado;
        }
        return null;
    }

    @Override public Void visitIdExpr(IdExprNode n) {
        Symbol s = n.decl;
        if (s == null) { n.type = Type.ERROR; return null; }
        if (s.kind == Kind.FUNC) {
            erro(n, "funcao '" + n.id + "' usada sem parenteses");
            n.type = Type.ERROR;
        } else {
            n.type = simboloTipo(s);
            if (!erroTipo(n.type) && !inicializado(s)) {
                aviso(n, "variavel '" + n.id + "' pode nao ter sido inicializada");
            }
        }
        return null;
    }

    @Override public Void visitIntLit(IntLitNode n) { n.type = Type.NUMBER; return null; }
    @Override public Void visitRealLit(RealLitNode n) { n.type = Type.REAL; return null; }
    @Override public Void visitCharLit(CharLitNode n) { n.type = Type.CHAR; return null; }
    @Override public Void visitStringLit(StringLitNode n) { n.type = Type.STRING; return null; }
    @Override public Void visitBoolLit(BoolLitNode n) { n.type = Type.BOOL; return null; }
}
