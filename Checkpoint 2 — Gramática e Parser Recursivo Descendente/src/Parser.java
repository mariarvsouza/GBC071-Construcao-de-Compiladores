import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser{
    private Scanner scanner;
    private Token lookahead;
    private List<String> erros;

    public Parser(Scanner scanner){
        this.scanner = scanner;
        this.erros = new ArrayList<>();
        this.lookahead = scanner.nextToken();
    }

    private String getTipoLookahead(){
        if (lookahead == null || lookahead.type == null) {
            return "EOF";
        }
        return lookahead.type.name();
    }

    private void match(String tipoEsperado){
        String tipoAtual = getTipoLookahead();
        if(lookahead != null && tipoAtual.equals(tipoEsperado)){
            lookahead = scanner.nextToken();
        }else{
            reportarErro("Esperava token " + tipoEsperado + ", mas encontrou " + tipoAtual);
        }
    }

    private void reportarErro(String mensagem){
        int linha = -1;
        int coluna = -1;
        erros.add("Erro sintático [Linha: " + linha + ", Coluna: " + coluna + "]: " + mensagem);
    }

    //Modo pânico : descarta tokens até encontrar um token pertencente ao conjunto de sincronização
    private void sincronizar(Set<String> syncSet){
        reportarErro("Entrando em modo pânico. Sincronizando...");
        while (lookahead != null && !getTipoLookahead().equals("EOF")) {
            if (syncSet.contains(getTipoLookahead())) {
                return;
            }
            lookahead = scanner.nextToken();
        }
    }

    public List<String> getErros(){
        return erros;
    }

    public boolean parse(){
        try{
            parsePrograma();
            if(lookahead != null && !getTipoLookahead().equals("EOF")){
                reportarErro("Tokens inesperados no final do arquivo.");
            }
        }catch (Exception e){
            erros.add("Erro fatal no parser: " + e.getMessage());
        }
        return erros.isEmpty();
    }

    //MÉTODOS RECURSIVOS DESCENDENTES POR NÂO - TERMINAL
    //-----------------------------------------------------

    //Programa -> StmtList
    private void parsePrograma(){
        parseStmtList();
    }

    //StmtList -> Stmt StmtList | ε (FOLLOW; RBRACE, EOF)
    private void parseStmtList(){
        while(lookahead != null && !getTipoLookahead().equals("EOF") && !lookahead.type.equals("RBRACE")){
            //Fisrt
            String t = getTipoLookahead();
            if(t.equals("KW_NUMBER") || t.equals("KW_REAL") || t.equals("KW_CHAR") || t.equals("KW_STR") || t.equals("KW_BOOL") || t.equals("KW_VOID") || t.equals("ID") || t.equals("KW_IF") || t.equals("KW_FOR") ||
                    t.equals("KW_BREAK") || t.equals("KW_RETURN") || t.equals("LBRACE")){
                parseStmt();
            }else{
                break;
            }
        }
    }

    //Stmt -> VarDecl | Assign | IfStmt | ForStmt | ReturnStmt | BreakStmt | Block
    private void parseStmt(){
        String t = getTipoLookahead();
        if(t.equals("KW_NUMBER") || t.equals("KW_REAL") || t.equals("KW_CHAR") ||
                t.equals("KW_STR") || t.equals("KW_BOOL") || t.equals("KW_VOID")) {
            parseVarDecl();
        }else if(t.equals("ID")){
            parseAssign();
        }else if(t.equals("KW_IF")){
            parseIfStmt();
        }else if(t.equals("KW_FOR")){
            parseForStmt();
        }else if (t.equals("KW_RETURN")){
            parseReturnStmt();
        }else if (t.equals("KW_BREAK")){
            parseBreakStmt();
        }else if (t.equals("LBRACE")){
            parseBlock();
        }else{
            reportarErro("Comando inválido. Token inesperado: " + t);
            Set<String> sync = new HashSet<>(List.of("SEMICOLON", "RBRACE"));
            sincronizar(sync);
        }
    }

    private void parseBlock(){
        match("LBRACE");
        parseStmtList();
        match("RBRACE");
    }

    private void parseVarDecl(){
        parseType();
        match("ID");
        parseVarDeclPrime();
    }

    private void parseVarDeclPrime(){
        String t = getTipoLookahead();
        if(t.equals("SEMICOLON")){
            match("SEMICOLON");
        }else if(t.equals("ASSIGN")){
            match("ASSIGN");
            parseExpr();
            match("SEMICOLON");
        }else{
            reportarErro("Esperado ';' ou '=' na declaração de varável");
            sincronizar(new HashSet<>(List.of("SEMICOLON", "RBRACE")));
        }
    }

    private void parseAssignBase(){
        match("ID");
        match("ASSIGN");
        parseExpr();
    }

    private void parseAssign(){
        parseAssignBase();
        match("SEMICOLON");
    }

    private void parseIfStmt(){
        match("KW_IF");
        match("LPAREN");
        parseExpr();
        match("RPAREN");
        parseBlock();
        parseIfStmtPrime();
    }

    private void parseIfStmtPrime(){
        if(lookahead != null && getTipoLookahead().equals("KW_ELSE")){
            match("KW_ELSE");
            parseBlock();
        }
    }

    private void parseForStmt(){
        match("KW_FOR");
        match("LPAREN");
        parseAssignBase();
        match("SEMICOLON");
        parseExpr();
        match("SEMICOLON");
        parseAssignBase();
        match("RPAREN");
        parseBlock();
    }

    private void parseReturnStmt(){
        match("KW_RETURN");
        parseReturnStmtPrime();
    }

    private void parseReturnStmtPrime(){
        if (getTipoLookahead().equals("SEMICOLON")) {
            match("SEMICOLON");
        } else {
            parseExpr();
            match("SEMICOLON");
        }
    }

    private void parseBreakStmt(){
        match("KW_BREAK");
        match("SEMICOLON");
    }

    private void parseType(){
        String t = getTipoLookahead();
        if(t.equals("KW_NUMBER") || t.equals("KW_REAL") || t.equals("KW_CHAR") || t.equals("KW_STR") || t.equals("KW_BOOL") || t.equals("KW_VOID")){
            match(t);
        }else{
            reportarErro("Tipo inválido: " + t);
            lookahead = scanner.nextToken();
        }
    }

    private void parseExpr(){
        parseOrExpr();
    }

    private void parseOrExpr(){
        parseAndExpr();
        parseOrExprPrime();
    }

    private void parseOrExprPrime(){
        while(lookahead != null && getTipoLookahead().equals("OR")){
            match("OR");
            parseAndExpr();
        }
    }

    private void parseAndExpr(){
        parseEqExpr();
        parseAndExprPrime();
    }

    private void parseAndExprPrime(){
        while(lookahead != null && getTipoLookahead().equals("AND")){
            match("AND");
            parseEqExpr();
        }
    }

    private void parseEqExpr(){
        parseRelExpr();
        parseEqExprPrime();
    }

    private void parseEqExprPrime(){
            while(lookahead != null && (getTipoLookahead().equals("EQ") || getTipoLookahead().equals("NE"))){
            String op = getTipoLookahead();
            match(op);
            parseRelExpr();
        }
    }

    private void parseRelExpr(){
        parseAddExpr();
        parseRelExprPrime();
    }

    private void parseRelExprPrime(){
        while (lookahead != null){
            String t = getTipoLookahead();
            if (t.equals("GT") || t.equals("GE") || t.equals("LT") || t.equals("LE")){
                match(t);
                parseAddExpr();
            }else{
                break;
            }
        }
    }

    private void parseAddExpr(){
        parseMultExpr();
        parseAddExprPrime();
    }

    private void parseAddExprPrime(){
        while (lookahead != null && (getTipoLookahead().equals("PLUS") || getTipoLookahead().equals("MINUS"))){
            String op = getTipoLookahead();
            match(op);
            parseMultExpr();
        }
    }

    private void parseMultExpr(){
        parseUnaryExpr();
        parseMultExprPrime();
    }

    private void parseMultExprPrime(){
        while (lookahead != null){
            String t = getTipoLookahead();
            if(t.equals("MULT") || t.equals("DIV") || t.equals("MOD")){
                match(t);
                parseUnaryExpr();
            }else{
                break;
            }
        }
    }

    private void parseUnaryExpr() {
        if (lookahead != null && getTipoLookahead().equals("NOT")) {
            match("NOT");
            parseUnaryExpr();
        } else {
            parseAtom();
        }
    }

    private void parseAtom() {
        if (lookahead == null) {
            reportarErro("Fim de arquivo inesperado dentro de expressão.");
            return;
        }
        String t = getTipoLookahead();
        if (t.equals("ID") || t.equals("NUMBER_LIT") || t.equals("REAL_LIT") ||
                t.equals("CHAR_LIT") || t.equals("STR_LIT")) {
            match(t);
        } else if (t.equals("LPAREN")) {
            match("LPAREN");
            parseExpr();
            match("RPAREN");
        } else {
            reportarErro("Átomo inválido: " + t);
            lookahead = scanner.nextToken(); // Consome para evitar loop infinito
        }
    }

}

