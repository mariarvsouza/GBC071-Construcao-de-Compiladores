import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {
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

    private void match(String tipoEsperado) {
        String tipoAtual = getTipoLookahead();
        if (lookahead != null && tipoAtual.equals(tipoEsperado)) {
            lookahead = scanner.nextToken();
        } else {
            reportarErro("Esperava token " + tipoEsperado + ", mas encontrou " + tipoAtual);
        }
    }

    private void reportarErro(String mensagem) {
        int linha = (lookahead != null) ? lookahead.line : -1;
        int coluna = (lookahead != null) ? lookahead.col : -1;
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

    public boolean parse() {
        try {
            parsePrograma();
            if (lookahead != null && !getTipoLookahead().equals("EOF")) {
                reportarErro("Tokens inesperados no final do arquivo.");
            }
        } catch (LexicalError e) {
            throw e;
        } catch (Exception e) {
            erros.add("Erro fatal no parser: " + e.getMessage());
        }
        return erros.isEmpty();
    }

    
    //parsers dos não-terminais:
    private void parsePrograma() {
        parseStmtList();
    }

    private void parseStmtList() {
        while (lookahead != null && !getTipoLookahead().equals("EOF") && !getTipoLookahead().equals("RBRACE")) {
            String t = getTipoLookahead();
            if (FIRST_TYPE.contains(t) || t.equals("ID") || t.equals("KW_IF") ||
                t.equals("KW_FOR") || t.equals("KW_BREAK") || t.equals("KW_RETURN") || t.equals("LBRACE")) {
                parseStmt();
            } else {
                reportarErro("Comando inválido ou token inesperado: " + t);
                sincronizar(SYNC_STMT_LIST);
                if (getTipoLookahead().equals("SEMICOLON")) {
                    lookahead = scanner.nextToken();
                }
            }
        }
    }

    private void parseStmt() {
        String t = getTipoLookahead();
        if (FIRST_TYPE.contains(t)) {
            parseDecl();
        } else if (t.equals("ID")) {
            match("ID");
            parseStmtIDPrime();
        } else if (t.equals("KW_IF")) {
            parseIfStmt();
        } else if (t.equals("KW_FOR")) {
            parseForStmt();
        } else if (t.equals("KW_RETURN")) {
            parseReturnStmt();
        } else if (t.equals("KW_BREAK")) {
            parseBreakStmt();
        } else if (t.equals("LBRACE")) {
            parseBlock();
        } else {
            reportarErro("Comando inválido: " + t);
            sincronizar(SYNC_STMT_LIST);
        }
    }
    
    private void parseStmtIDPrime() {
        String t = getTipoLookahead();
        if (t.equals("ASSIGN")) {
            match("ASSIGN");
            parseExpr();
            match("SEMICOLON");
        } else if (t.equals("LPAREN")) {
            match("LPAREN");
            parseArgList();
            match("RPAREN");
            match("SEMICOLON");
        } else {
            reportarErro("Esperado '=' ou '(' após identificador, encontrou: " + t);
            sincronizar(SYNC_STMT_LIST);
        }
    }

    private void parseDecl() {
        parseType();
        match("ID");
        parseDeclPrime();
    }

    private void parseDeclPrime() {
        String t = getTipoLookahead();
        if (t.equals("LPAREN")) {
            match("LPAREN");
            parseParamList();
            match("RPAREN");
            parseBlock();
        } else if (t.equals("SEMICOLON") || t.equals("ASSIGN")) {
            parseVarDeclPrime();
        } else {
            reportarErro("Esperado '(' para declaração de função ou ';' / '=' para variável, encontrou: " + t);
            sincronizar(FOLLOW_IF_STMT_PRIME);
        }
    }

    private void parseVarDeclPrime() {
        String t = getTipoLookahead();
        if (t.equals("SEMICOLON")) {
            match("SEMICOLON");
        } else if (t.equals("ASSIGN")) {
            match("ASSIGN");
            parseExpr();
            match("SEMICOLON");
        } else {
            reportarErro("Esperado ';' ou '=' na declaração de variável");
            sincronizar(FOLLOW_IF_STMT_PRIME);
        }
    }

    private void parseParamList() {
        String t = getTipoLookahead();
        if (FIRST_TYPE.contains(t)) {
            parseParam();
            parseParamListPrime();
        } else if (FOLLOW_PARAM_LIST.contains(t) || t.equals("EOF")) {
            return; 
        } else {
            reportarErro("Esperava parâmetro ou ')', encontrou: " + t);
            sincronizar(FOLLOW_PARAM_LIST);
        }
    }

    private void parseParamListPrime() {
        String t = getTipoLookahead();
        if (t.equals("COMMA")) {
            match("COMMA");
            parseParam();
            parseParamListPrime();
        } else if (FOLLOW_PARAM_LIST.contains(t) || t.equals("EOF")) {
            return;
        } else {
            reportarErro("Esperava ',' ou ')', encontrou: " + t);
            sincronizar(FOLLOW_PARAM_LIST);
        }
    }

    private void parseParam() {
        parseType();
        match("ID");
    }

    private void parseBlock() {
        match("LBRACE");
        parseStmtList();
        match("RBRACE");
    }

    private void parseAssignBase() {
        match("ID");
        match("ASSIGN");
        parseExpr();
    }

    private void parseAssign() {
        parseAssignBase();
        match("SEMICOLON");
    }

    private void parseIfStmt() {
        match("KW_IF");
        match("LPAREN");
        parseExpr();
        match("RPAREN");
        parseBlock();
        parseIfStmtPrime();
    }

    private void parseIfStmtPrime() {
        String t = getTipoLookahead();
        if (t.equals("KW_ELSE")) {
            match("KW_ELSE");
            parseBlock();
        } else if (FOLLOW_IF_STMT_PRIME.contains(t) || t.equals("EOF")) {
            return;
        } else {
            reportarErro("Token inesperado após if: " + t);
            sincronizar(FOLLOW_IF_STMT_PRIME);
        }
    }

    private void parseForStmt() {
        match("KW_FOR");
        match("LPAREN");
        parseForInit();
        match("SEMICOLON");
        parseExpr();
        match("SEMICOLON");
        parseAssignBase();
        match("RPAREN");
        parseBlock();
    }
    
    private void parseForInit() {
        String t = getTipoLookahead();
        if (FIRST_TYPE.contains(t)) {
            parseType();
            match("ID");
            match("ASSIGN");
            parseExpr();
        } else if (t.equals("ID")) {
            parseAssignBase();
        } else {
            reportarErro("Inicialização inválida no 'for'.");
            sincronizar(FOLLOW_FOR_INIT);
        }
    }

    private void parseReturnStmt() {
        match("KW_RETURN");
        parseReturnStmtPrime();
    }

    private void parseReturnStmtPrime() {
        String t = getTipoLookahead();
        if (t.equals("SEMICOLON")) {
            match("SEMICOLON");
        } else if (FIRST_EXPR.contains(t)) {
            parseExpr();
            match("SEMICOLON");
        } else {
            reportarErro("Esperada expressão ou ';' após return, encontrou " + t);
            sincronizar(FOLLOW_RETURN_STMT_PRIME);
        }
    }

    private void parseBreakStmt() {
        match("KW_BREAK");
        match("SEMICOLON");
    }

    private void parseType() {
        String t = getTipoLookahead();
        if (FIRST_TYPE.contains(t)) {
            match(t);
        } else {
            reportarErro("Tipo inválido: " + t);
            sincronizar(FOLLOW_TYPE);
        }
    }

    private void parseExpr() {
        parseOrExpr();
    }

    private void parseOrExpr() {
        parseAndExpr();
        parseOrExprPrime();
    }

    private void parseOrExprPrime() {
        String t = getTipoLookahead();
        if (t.equals("OR")) {
            match("OR");
            parseAndExpr();
            parseOrExprPrime();
        } else if (FOLLOW_OR_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return;
        } else {
            reportarErro("Token inesperado em expressão OR: " + t);
            sincronizar(FOLLOW_OR_EXPR_PRIME);
        }
    }

    private void parseAndExpr() {
        parseEqExpr();
        parseAndExprPrime();
    }

    private void parseAndExprPrime() {
        String t = getTipoLookahead();
        if (t.equals("AND")) {
            match("AND");
            parseEqExpr();
            parseAndExprPrime();
        } else if (FOLLOW_AND_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return;
        } else {
            reportarErro("Token inesperado em expressão AND: " + t);
            sincronizar(FOLLOW_AND_EXPR_PRIME);
        }
    }

    private void parseEqExpr() {
        parseRelExpr();
        parseEqExprPrime();
    }

    private void parseEqExprPrime() {
        String t = getTipoLookahead();
        if (t.equals("EQ") || t.equals("NE")) {
            match(t);
            parseRelExpr();
            parseEqExprPrime();
        } else if (FOLLOW_EQ_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return;
        } else {
            reportarErro("Token inesperado em expressão de igualdade: " + t);
            sincronizar(FOLLOW_EQ_EXPR_PRIME);
        }
    }

    private void parseRelExpr() {
        parseAddExpr();
        parseRelExprPrime();
    }

    private void parseRelExprPrime() {
        String t = getTipoLookahead();
        if (t.equals("GT") || t.equals("GE") || t.equals("LT") || t.equals("LE")) {
            match(t);
            parseAddExpr();
            parseRelExprPrime();
        } else if (FOLLOW_REL_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return;
        } else {
            reportarErro("Token inesperado em expressão relacional: " + t);
            sincronizar(FOLLOW_REL_EXPR_PRIME);
        }
    }

    private void parseAddExpr() {
        parseMultExpr();
        parseAddExprPrime();
    }

    private void parseAddExprPrime() {
        String t = getTipoLookahead();
        if (t.equals("PLUS") || t.equals("MINUS")) {
            match(t);
            parseMultExpr();
            parseAddExprPrime();
        } else if (FOLLOW_ADD_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return;
        } else {
            reportarErro("Token inesperado em expressão de adição: " + t);
            sincronizar(FOLLOW_ADD_EXPR_PRIME);
        }
    }

    private void parseMultExpr() {
        parseUnaryExpr();
        parseMultExprPrime();
    }

    private void parseMultExprPrime() {
        String t = getTipoLookahead();
        if (t.equals("MULT") || t.equals("DIV") || t.equals("MOD")) {
            match(t);
            parseUnaryExpr();
            parseMultExprPrime();
        } else if (FOLLOW_MULT_EXPR_PRIME.contains(t) || t.equals("EOF")) {
            return;
        } else {
            reportarErro("Token inesperado em expressão de multiplicação: " + t);
            sincronizar(FOLLOW_MULT_EXPR_PRIME);
        }
    }

    private void parseUnaryExpr() {
        String t = getTipoLookahead();
        if (t.equals("NOT")) {
            match("NOT");
            parseUnaryExpr();
        } else if (FIRST_EXPR.contains(t)) {
            parseAtom();
        } else {
            reportarErro("Operador unário ou átomo esperado, encontrou: " + t);
            sincronizar(FOLLOW_MULT_EXPR_PRIME);
        }
    }

    private void parseAtom() {
        if (lookahead == null) {
            reportarErro("Fim de arquivo inesperado dentro de expressão.");
            return;
        }
        String t = getTipoLookahead();
        if (t.equals("ID")) {
            match("ID");
            parseAtomPrime();
        } else if (t.equals("NUMBER_LIT") || t.equals("REAL_LIT") || t.equals("CHAR_LIT") ||
                   t.equals("STR_LIT") || t.equals("TRUE_LIT") || t.equals("FALSE_LIT")) {
            match(t);
        } else if (t.equals("LPAREN")) {
            match("LPAREN");
            parseExpr();
            match("RPAREN");
        } else {
            reportarErro("Átomo inválido: " + t);
            sincronizar(FOLLOW_ATOM);
        }
    }

    private void parseAtomPrime() {
        String t = getTipoLookahead();
        if (t.equals("LPAREN")) {
            match("LPAREN");
            parseArgList();
            match("RPAREN");
        } else if (FOLLOW_ATOM.contains(t) || t.equals("EOF")) {
            return; 
        } else {
            reportarErro("Token inesperado após identificador em expressão: " + t);
            sincronizar(FOLLOW_ATOM);
        }
    }

    private void parseArgList() {
        String t = getTipoLookahead();
        if (FIRST_EXPR.contains(t)) {
            parseExpr();
            parseArgListPrime();
        } else if (FOLLOW_ARG_LIST.contains(t) || t.equals("EOF")) {
            return;
        } else {
            reportarErro("Esperava expressão ou ')', encontrou: " + t);
            sincronizar(FOLLOW_ARG_LIST);
        }
    }

    private void parseArgListPrime() {
        String t = getTipoLookahead();
        if (t.equals("COMMA")) {
            match("COMMA");
            parseExpr();
            parseArgListPrime();
        } else if (FOLLOW_ARG_LIST.contains(t) || t.equals("EOF")) {
            return; // ε
        } else {
            reportarErro("Esperava ',' ou ')', encontrou: " + t);
            sincronizar(FOLLOW_ARG_LIST);
        }
    }
}