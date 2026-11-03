import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {
    public static class ParseResult {
        public final ProgramNode ast;
        public final List<String> erros;
        public ParseResult(ProgramNode ast, List<String> erros) {
            if (erros != null) {
                this.erros = erros;
            } else {
                this.erros = new ArrayList<>();
            }
            if (this.erros.isEmpty()) {
                this.ast = ast;
            } else {
                this.ast = null;
            }
        }

        public boolean isSucesso() {
            return erros.isEmpty() && ast != null;
        }
    }
    
    private Scanner scanner;
    private Token lookahead;
    private List<String> erros;

    //conjuntos FIRST e FOLLOW:
    private static final Set<String> FIRST_TYPE = new HashSet<>(List.of("KW_NUMBER", "KW_REAL", "KW_CHAR", "KW_STR", "KW_BOOL", "KW_VOID"));
    private static final Set<String> FIRST_EXPR = new HashSet<>(List.of("NOT", "ID", "NUMBER_LIT", "REAL_LIT", "CHAR_LIT", "STR_LIT", "TRUE_LIT", "FALSE_LIT", "LPAREN"));
    private static final Set<String> FOLLOW_TYPE = new HashSet<>(List.of("ID", "RPAREN", "COMMA", "SEMICOLON", "LBRACE"));
    private static final Set<String> FOLLOW_IF_STMT_PRIME = new HashSet<>(List.of(
            "KW_NUMBER", "KW_REAL", "KW_CHAR", "KW_STR", "KW_BOOL", "KW_VOID",
            "ID", "KW_IF", "KW_FOR", "KW_RETURN", "KW_BREAK", "LBRACE", "RBRACE", "EOF"
    ));
    private static final Set<String> FOLLOW_RETURN_STMT_PRIME = new HashSet<>(List.of(
            "KW_NUMBER", "KW_REAL", "KW_CHAR", "KW_STR", "KW_BOOL", "KW_VOID",
            "ID", "KW_IF", "KW_FOR", "KW_RETURN", "KW_BREAK", "LBRACE", "RBRACE", "EOF"
    ));
    private static final Set<String> FOLLOW_FOR_INIT = new HashSet<>(List.of("SEMICOLON"));
    private static final Set<String> FOLLOW_PARAM_LIST = new HashSet<>(List.of("RPAREN"));
    private static final Set<String> FOLLOW_ARG_LIST = new HashSet<>(List.of("RPAREN"));
    private static final Set<String> FOLLOW_OR_EXPR_PRIME = new HashSet<>(List.of("SEMICOLON", "RPAREN", "COMMA", "LBRACE"));
    private static final Set<String> FOLLOW_AND_EXPR_PRIME = new HashSet<>(List.of("OR", "SEMICOLON", "RPAREN", "COMMA"));
    private static final Set<String> FOLLOW_EQ_EXPR_PRIME = new HashSet<>(List.of("AND", "OR", "SEMICOLON", "RPAREN", "COMMA"));
    private static final Set<String> FOLLOW_REL_EXPR_PRIME = new HashSet<>(List.of("EQ", "NE", "AND", "OR", "SEMICOLON", "RPAREN", "COMMA"));
    private static final Set<String> FOLLOW_ADD_EXPR_PRIME = new HashSet<>(List.of("GT", "GE", "LT", "LE", "EQ", "NE", "AND", "OR", "SEMICOLON", "RPAREN", "COMMA"));
    private static final Set<String> FOLLOW_MULT_EXPR_PRIME = new HashSet<>(List.of("PLUS", "MINUS", "GT", "GE", "LT", "LE", "EQ", "NE", "AND", "OR", "SEMICOLON", "RPAREN", "COMMA"));
    private static final Set<String> FOLLOW_ATOM = new HashSet<>(List.of("MULT", "DIV", "MOD", "PLUS", "MINUS", "GT", "GE", "LT", "LE", "EQ", "NE", "AND", "OR", "SEMICOLON", "RPAREN", "COMMA"));
    private static final Set<String> SYNC_STMT_LIST = new HashSet<>(List.of(
            "KW_NUMBER", "KW_REAL", "KW_CHAR", "KW_STR", "KW_BOOL", "KW_VOID",
            "ID", "KW_IF", "KW_FOR", "KW_RETURN", "KW_BREAK", "LBRACE", "SEMICOLON", "RBRACE"
    ));

    //construtor:
    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.erros = new ArrayList<>();
        this.lookahead = scanner.nextToken();
    }

    private String getTipoLookahead() {
        if (lookahead == null || lookahead.type == null) {
            return "EOF";
        }
        return lookahead.type.name();
    }

    private Token match(String tipoEsperado) {
        Token t = lookahead;
        String tipoAtual = getTipoLookahead();
        if (lookahead != null && tipoAtual.equals(tipoEsperado)) {
            lookahead = scanner.nextToken();
            return t;
        } else {
            reportarErro("Esperava token " + tipoEsperado + ", mas encontrou " + tipoAtual);
            if (t != null) {
                return t;
            } else {
                return new Token(null, "", -1, -1);
            }
        }
    }

    private void reportarErro(String mensagem) {
        int linha;
        int coluna;
        if (lookahead != null) {
            linha = lookahead.line;
            coluna = lookahead.col;
        } else {
            linha = -1;
            coluna = -1;
        }
        erros.add("Erro sintático [Linha: " + linha + ", Coluna: " + coluna + "]: " + mensagem);
    }

    private void sincronizar(Set<String> syncSet) {
        while (lookahead != null && !getTipoLookahead().equals("EOF")) {
            if (syncSet.contains(getTipoLookahead())) {
                return;
            }
            lookahead = scanner.nextToken();
        }
    }

    public List<String> getErros() {
        return erros;
    }

    public ParseResult parse() {
        ProgramNode raiz = null;
        try {
            raiz = parsePrograma();
            if (lookahead != null && !getTipoLookahead().equals("EOF")) {
                reportarErro("Tokens inesperados no final do arquivo.");
            }
        } catch (LexicalError e) {
            throw e;
        } catch (Exception e) {
            erros.add("Erro fatal no parser: " + e.getMessage());
        }
        return new ParseResult(raiz, erros);
    }

    
    //parsers dos não-terminais:
    private ProgramNode parsePrograma() {
        int l;
        int c;
        if (lookahead != null) {
            l = lookahead.line;
            c = lookahead.col;
        } else {
            l = 1;
            c = 1;
        }
        List<StmtNode> stmts = parseStmtList();
        return new ProgramNode(stmts, l, c);
    }

    private List<StmtNode> parseStmtList() {
        List<StmtNode> stmts = new ArrayList<>();
        while (lookahead != null && !getTipoLookahead().equals("EOF") && !getTipoLookahead().equals("RBRACE")) {
            String t = getTipoLookahead();
            if (FIRST_TYPE.contains(t) || t.equals("ID") || t.equals("KW_IF") || t.equals("KW_FOR") || t.equals("KW_BREAK") || t.equals("KW_RETURN") || t.equals("LBRACE")) {
                StmtNode stmt = parseStmt();
                if (stmt != null) stmts.add(stmt);
            } else {
                reportarErro("Comando inválido ou token inesperado: " + t);
                sincronizar(SYNC_STMT_LIST);
                if (getTipoLookahead().equals("SEMICOLON")) {
                    lookahead = scanner.nextToken();
                }
            }
        }
        return stmts;
    }

    private StmtNode parseStmt() {
        String t = getTipoLookahead();
        if (FIRST_TYPE.contains(t)) {
            return parseDecl();
        } else if (t.equals("ID")) {
            Token idTok = match("ID");
            return parseStmtIDPrime(idTok);
        } else if (t.equals("KW_IF")) {
            return parseIfStmt();
        } else if (t.equals("KW_FOR")) {
            return parseForStmt();
        } else if (t.equals("KW_RETURN")) {
            return parseReturnStmt();
        } else if (t.equals("KW_BREAK")) {
            return parseBreakStmt();
        } else if (t.equals("LBRACE")) {
            return parseBlock();
        } else {
            reportarErro("Comando inválido: "+t);
            sincronizar(SYNC_STMT_LIST);
            return null;
        }
    }
    
    private StmtNode parseStmtIDPrime(Token idTok) {
        String t = getTipoLookahead();
        if (t.equals("ASSIGN")) {
            match("ASSIGN");
            ExprNode expr = parseExpr();
            match("SEMICOLON");
            return new AssignNode(idTok.lexeme, expr, idTok.line, idTok.col);
        } else if (t.equals("LPAREN")) {
            match("LPAREN");
            List<ExprNode> args = parseArgList();
            match("RPAREN");
            match("SEMICOLON");
            return new CallFuncExprNode(idTok.lexeme, args, idTok.line, idTok.col);
        } else {
            reportarErro("Esperado '=' ou '(' após identificador, encontrou: "+t);
            sincronizar(SYNC_STMT_LIST);
            return null;
        }
    }
    private StmtNode parseDecl() {
        Token typeTok = parseType();
        Token idTok = match("ID");
        return parseDeclPrime(typeTok.lexeme, idTok);
    }

    private StmtNode parseDeclPrime(String type, Token idTok) {
        String t = getTipoLookahead();
        if (t.equals("LPAREN")) {
            match("LPAREN");
            List<ParamNode> params = parseParamList();
            match("RPAREN");
            BlockNode body = parseBlock();
            return new FuncDeclNode(type, idTok.lexeme, params, body, idTok.line, idTok.col);
        } else if (t.equals("SEMICOLON") || t.equals("ASSIGN")) {
            ExprNode initExpr = parseVarDeclPrime();
            return new VarDeclNode(type, idTok.lexeme, initExpr, idTok.line, idTok.col);
        } else {
            reportarErro("Esperado '(' para declaração de função ou ';' / '=' para variável, encontrou: "+t);
            sincronizar(FOLLOW_IF_STMT_PRIME);
            return null;
        }
    }

    private ExprNode parseVarDeclPrime() {
        String t = getTipoLookahead();
        if (t.equals("SEMICOLON")) {
            match("SEMICOLON");
            return null;
        } else if (t.equals("ASSIGN")) {
            match("ASSIGN");
            ExprNode expr = parseExpr();
            match("SEMICOLON");
            return expr;
        } else {
            reportarErro("Esperado ';' ou '=' na declaração de variável");
            sincronizar(FOLLOW_IF_STMT_PRIME);
            return null;
        }
    }

    private List<ParamNode> parseParamList() {
        List<ParamNode> params = new ArrayList<>();
        String t = getTipoLookahead();
        if (FIRST_TYPE.contains(t)) {
            ParamNode p = parseParam();
            if (p != null) params.add(p);
            parseParamListPrime(params);
        } else if (FOLLOW_PARAM_LIST.contains(t) || t.equals("EOF")) {
            //vazio
        } else {
            reportarErro("Esperava parâmetro ou ')', encontrou: "+t);
            sincronizar(FOLLOW_PARAM_LIST);
        }
        return params;
    }

    private void parseParamListPrime(List<ParamNode> params) {
        String t = getTipoLookahead();
        if (t.equals("COMMA")) {
            match("COMMA");
            ParamNode p = parseParam();
            if (p != null) params.add(p);
            parseParamListPrime(params);
        } else if (FOLLOW_PARAM_LIST.contains(t) || t.equals("EOF")) {
            //vazio
        } else {
            reportarErro("Esperava ',' ou ')', encontrou: "+t);
            sincronizar(FOLLOW_PARAM_LIST);
        }
    }

    private ParamNode parseParam() {
        Token typeTok = parseType();
        Token idTok = match("ID");
        return new ParamNode(typeTok.lexeme, idTok.lexeme, typeTok.line, typeTok.col);
    }

    private BlockNode parseBlock() {
        Token lbraceTok = match("LBRACE");
        List<StmtNode> stmts = parseStmtList();
        match("RBRACE");
        return new BlockNode(stmts, lbraceTok.line, lbraceTok.col);
    }

    private AssignNode parseAssignBase() {
        Token idTok = match("ID");
        match("ASSIGN");
        ExprNode expr = parseExpr();
        return new AssignNode(idTok.lexeme, expr, idTok.line, idTok.col);
    }

    private AssignNode parseAssign() {
        AssignNode assign = parseAssignBase();
        match("SEMICOLON");
        return assign;
    }

    private IfStmtNode parseIfStmt() {
        Token ifTok = match("KW_IF");
        match("LPAREN");
        ExprNode cond = parseExpr();
        match("RPAREN");
        BlockNode thenBlock = parseBlock();
        BlockNode elseBlock = parseIfStmtPrime();
        return new IfStmtNode(cond, thenBlock, elseBlock, ifTok.line, ifTok.col);
    }

    private BlockNode parseIfStmtPrime() {
        String t = getTipoLookahead();
        if (t.equals("KW_ELSE")) {
            match("KW_ELSE");
            return parseBlock();
        } else if (FOLLOW_IF_STMT_PRIME.contains(t) || t.equals("EOF")) {
            return null;
        } else {
            reportarErro("Token inesperado após if: "+t);
            sincronizar(FOLLOW_IF_STMT_PRIME);
            return null;
        }
    }

    private ForStmtNode parseForStmt() {
        Token forTok = match("KW_FOR");
        match("LPAREN");
        StmtNode init = parseForInit();
        match("SEMICOLON");
        ExprNode cond = parseExpr();
        match("SEMICOLON");
        AssignNode inc = parseAssignBase();
        match("RPAREN");
        BlockNode body = parseBlock();
        return new ForStmtNode(init, cond, inc, body, forTok.line, forTok.col);
    }
    
    private StmtNode parseForInit() {
        String t = getTipoLookahead();
        if (FIRST_TYPE.contains(t)) {
            Token typeTok = parseType();
            Token idTok = match("ID");
            match("ASSIGN");
            ExprNode expr = parseExpr();
            return new VarDeclNode(typeTok.lexeme, idTok.lexeme, expr, typeTok.line, typeTok.col);
        } else if (t.equals("ID")) {
            return parseAssignBase();
        } else {
            reportarErro("Inicialização inválida no 'for'.");
            sincronizar(FOLLOW_FOR_INIT);
            return null;
        }
    }

    private ReturnStmtNode parseReturnStmt() {
        Token retTok = match("KW_RETURN");
        ExprNode expr = parseReturnStmtPrime();
        return new ReturnStmtNode(expr, retTok.line, retTok.col);
    }

    private ExprNode parseReturnStmtPrime() {
        String t = getTipoLookahead();
        if (t.equals("SEMICOLON")) {
            match("SEMICOLON");
            return null;
        } else if (FIRST_EXPR.contains(t)) {
            ExprNode expr = parseExpr();
            match("SEMICOLON");
            return expr;
        } else {
            reportarErro("Esperada expressão ou ';' após return, encontrou " + t);
            sincronizar(FOLLOW_RETURN_STMT_PRIME);
            return null;
        }
    }

    private BreakStmtNode parseBreakStmt() {
        Token breakTok = match("KW_BREAK");
        match("SEMICOLON");
        return new BreakStmtNode(breakTok.line, breakTok.col);
    }

    private Token parseType() {
        String t = getTipoLookahead();
        if (FIRST_TYPE.contains(t)) {
            return match(t);
        } else {
            reportarErro("Tipo inválido: "+t);
            sincronizar(FOLLOW_TYPE);
            int linha;
            int coluna;
            if (lookahead != null) {
                linha = lookahead.line;
                coluna = lookahead.col;
            } else {
                linha = -1;
                coluna = -1;
            }
            return new Token(null, "void", linha, coluna);
        }
    }

    private ExprNode parseExpr() {
        return parseOrExpr();
    }

    private ExprNode parseOrExpr() {
        ExprNode left = parseAndExpr();
        return parseOrExprPrime(left);
    }

    private ExprNode parseOrExprPrime(ExprNode left) {
        String t = getTipoLookahead();
        if (t.equals("OR")) {
            Token opTok = match("OR");
            ExprNode right = parseAndExpr();
            ExprNode newLeft = new BinExprNode(left, opTok.lexeme, right, opTok.line, opTok.col);
            return parseOrExprPrime(newLeft);
        } else if (FOLLOW_OR_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return left;
        } else {
            reportarErro("Token inesperado em expressão OR: "+t);
            sincronizar(FOLLOW_OR_EXPR_PRIME);
            return left;
        }
    }

    private ExprNode parseAndExpr() {
        ExprNode left = parseEqExpr();
        return parseAndExprPrime(left);
    }

    private ExprNode parseAndExprPrime(ExprNode left) {
        String t = getTipoLookahead();
        if (t.equals("AND")) {
            Token opTok = match("AND");
            ExprNode right = parseEqExpr();
            ExprNode newLeft = new BinExprNode(left, opTok.lexeme, right, opTok.line, opTok.col);
            return parseAndExprPrime(newLeft);
        } else if (FOLLOW_AND_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return left;
        } else {
            reportarErro("Token inesperado em expressão AND: "+t);
            sincronizar(FOLLOW_AND_EXPR_PRIME);
            return left;
        }
    }

    private ExprNode parseEqExpr() {
        ExprNode left = parseRelExpr();
        return parseEqExprPrime(left);
    }

    private ExprNode parseEqExprPrime(ExprNode left) {
        String t = getTipoLookahead();
        if (t.equals("EQ") || t.equals("NE")) {
            Token opTok = match(t);
            ExprNode right = parseRelExpr();
            ExprNode newLeft = new BinExprNode(left, opTok.lexeme, right, opTok.line, opTok.col);
            return parseEqExprPrime(newLeft);
        } else if (FOLLOW_EQ_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return left;
        } else {
            reportarErro("Token inesperado em expressão de igualdade: "+t);
            sincronizar(FOLLOW_EQ_EXPR_PRIME);
            return left;
        }
    }

    private ExprNode parseRelExpr() {
        ExprNode left = parseAddExpr();
        return parseRelExprPrime(left);
    }

    private ExprNode parseRelExprPrime(ExprNode left) {
        String t = getTipoLookahead();
        if (t.equals("GT") || t.equals("GE") || t.equals("LT") || t.equals("LE")) {
            Token opTok = match(t);
            ExprNode right = parseAddExpr();
            ExprNode newLeft = new BinExprNode(left, opTok.lexeme, right, opTok.line, opTok.col);
            return parseRelExprPrime(newLeft);
        } else if (FOLLOW_REL_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return left;
        } else {
            reportarErro("Token inesperado em expressão relacional: "+t);
            sincronizar(FOLLOW_REL_EXPR_PRIME);
            return left;
        }
    }

    private ExprNode parseAddExpr() {
        ExprNode left = parseMultExpr();
        return parseAddExprPrime(left);
    }

    private ExprNode parseAddExprPrime(ExprNode left) {
        String t = getTipoLookahead();
        if (t.equals("PLUS") || t.equals("MINUS")) {
            Token opTok = match(t);
            ExprNode right = parseMultExpr();
            ExprNode newLeft = new BinExprNode(left, opTok.lexeme, right, opTok.line, opTok.col);
            return parseAddExprPrime(newLeft);
        } else if (FOLLOW_ADD_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return left;
        } else {
            reportarErro("Token inesperado em expressão de adição: "+t);
            sincronizar(FOLLOW_ADD_EXPR_PRIME);
            return left;
        }
    }

    private ExprNode parseMultExpr() {
        ExprNode left = parseUnaryExpr();
        return parseMultExprPrime(left);
    }

    private ExprNode parseMultExprPrime(ExprNode left) {
        String t = getTipoLookahead();
        if (t.equals("MULT") || t.equals("DIV") || t.equals("MOD")) {
            Token opTok = match(t);
            ExprNode right = parseUnaryExpr();
            ExprNode newLeft = new BinExprNode(left, opTok.lexeme, right, opTok.line, opTok.col);
            return parseMultExprPrime(newLeft);
        } else if (FOLLOW_MULT_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return left;
        } else {
            reportarErro("Token inesperado em expressão de multiplicação: "+t);
            sincronizar(FOLLOW_MULT_EXPR_PRIME);
            return left;
        }
    }

    private ExprNode parseUnaryExpr() {
        String t = getTipoLookahead();
        if (t.equals("NOT")) {
            Token opTok = match("NOT");
            ExprNode operand = parseUnaryExpr();
            return new UnaryExprNode(opTok.lexeme, operand, opTok.line, opTok.col);
        } else if (FIRST_EXPR.contains(t)) {
            return parseAtom();
        } else {
            reportarErro("Operador unário ou átomo esperado, encontrou: "+t);
            sincronizar(FOLLOW_MULT_EXPR_PRIME);
            return null;
        }
    }

    private ExprNode parseAtom() {
        if (lookahead == null) {
            reportarErro("Fim de arquivo inesperado dentro de expressão.");
            return null;
        }
        String t = getTipoLookahead();
        if (t.equals("ID")) {
            Token idTok = match("ID");
            return parseAtomPrime(idTok);
        } else if (t.equals("NUMBER_LIT")) {
            Token tok = match("NUMBER_LIT");
            int val = 0;
            try { val = Integer.parseInt(tok.lexeme); } catch (Exception ignored) {}
            return new IntLitNode(val, tok.line, tok.col);
        } else if (t.equals("REAL_LIT")) {
            Token tok = match("REAL_LIT");
            double val = 0.0;
            try { val = Double.parseDouble(tok.lexeme); } catch (Exception ignored) {}
            return new RealLitNode(val, tok.line, tok.col);
        } else if (t.equals("CHAR_LIT")) {
            Token tok = match("CHAR_LIT");
            char val;
            if (tok.lexeme != null && !tok.lexeme.isEmpty()) {
                val = tok.lexeme.charAt(0);
            } else {
                val = '\0';
            }
            return new CharLitNode(val, tok.line, tok.col);
        } else if (t.equals("STR_LIT")) {
            Token tok = match("STR_LIT");
            return new StringLitNode(tok.lexeme, tok.line, tok.col);
        } else if (t.equals("TRUE_LIT") || t.equals("FALSE_LIT")) {
            Token tok = match(t);
            boolean val = t.equals("TRUE_LIT");
            return new BoolLitNode(val, tok.line, tok.col);
        } else if (t.equals("LPAREN")) {
            match("LPAREN");
            ExprNode expr = parseExpr();
            match("RPAREN");
            return expr;
        } else {
            reportarErro("Átomo inválido: "+t);
            sincronizar(FOLLOW_ATOM);
            return null;
        }
    }

    private ExprNode parseAtomPrime(Token idTok) {
        String t = getTipoLookahead();
        if (t.equals("LPAREN")) {
            match("LPAREN");
            List<ExprNode> args = parseArgList();
            match("RPAREN");
            return new CallFuncExprNode(idTok.lexeme, args, idTok.line, idTok.col);
        } else if (FOLLOW_ATOM.contains(t) || t.equals("EOF")) {
            return new IdExprNode(idTok.lexeme, idTok.line, idTok.col);
        } else {
            reportarErro("Token inesperado após identificador em expressão: "+t);
            sincronizar(FOLLOW_ATOM);
            return new IdExprNode(idTok.lexeme, idTok.line, idTok.col);
        }
    }

    private List<ExprNode> parseArgList() {
        List<ExprNode> args = new ArrayList<>();
        String t = getTipoLookahead();
        if (FIRST_EXPR.contains(t)) {
            ExprNode expr = parseExpr();
            if (expr != null) args.add(expr);
            parseArgListPrime(args);
        } else if (FOLLOW_ARG_LIST.contains(t) || t.equals("EOF")) {
            //vazio
        } else {
            reportarErro("Esperava expressão ou ')', encontrou: "+t);
            sincronizar(FOLLOW_ARG_LIST);
        }
        return args;
    }

    private void parseArgListPrime(List<ExprNode> args) {
        String t = getTipoLookahead();
        if (t.equals("COMMA")) {
            match("COMMA");
            ExprNode expr = parseExpr();
            if (expr != null) args.add(expr);
            parseArgListPrime(args);
        } else if (FOLLOW_ARG_LIST.contains(t) || t.equals("EOF")) {
            //vazio
        } else {
            reportarErro("Esperava ',' ou ')', encontrou: "+t);
            sincronizar(FOLLOW_ARG_LIST);
        }
    }
}