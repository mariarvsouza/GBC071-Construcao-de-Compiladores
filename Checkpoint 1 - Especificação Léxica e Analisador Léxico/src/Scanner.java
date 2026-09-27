import java.util.Map;
import java.util.HashMap;

public class Scanner {
    private String source;
    private Map<String, TokenType> keywords = new HashMap<>();
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    public Scanner(String source) {
        this.source = source;
        keywords.put("number", TokenType.KW_NUMBER);
        keywords.put("real", TokenType.KW_REAL);
        keywords.put("char", TokenType.KW_CHAR);
        keywords.put("string", TokenType.KW_STR);
        keywords.put("bool", TokenType.KW_BOOL);
        keywords.put("if", TokenType.KW_IF);
        keywords.put("else", TokenType.KW_ELSE);
        keywords.put("return", TokenType.KW_RETURN);
        keywords.put("void", TokenType.KW_VOID);
        keywords.put("break", TokenType.KW_BREAK);
        keywords.put("for", TokenType.KW_FOR);
    }

    private boolean hasNext() {
        return pos<source.length();
    }

    private char peek() {
        if (!hasNext()) return '\0';
        return source.charAt(pos);
    }

    private char advance() {
        char c = source.charAt(pos++);
        if (c=='\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        return c;
    }

    private void skipWhitespaceAndComments() {
        while (hasNext()) {
            char c = peek();
            if (c==' ' || c=='\t' || c=='\n') advance();
            //pula comentários:
            //de bloco:
            else if (c=='-' && pos+1<source.length() && source.charAt(pos+1)=='>') {
                int startCommentLine = line;
                int startCommentCol = col;
                advance(); 
                advance(); 
                boolean closed = false;
                while (hasNext()) {
                    if (peek()=='<' && pos+1<source.length() && source.charAt(pos+1)=='-') {
                        advance(); 
                        advance();
                        closed = true;
                        break;
                    }
                    advance();
                }
                if(closed==false) {
                    throw new LexicalError("Comentário de bloco não terminado", startCommentLine, startCommentCol);
                }
            }
            //de linha:
            else if (c=='-' && pos+2<source.length() && source.charAt(pos+1)=='-' && source.charAt(pos+2)=='>') {
                advance();
                advance();
                advance();
                while (hasNext()) {
                    char current = peek();
                    if (current == '\n') {
                        break;
                    }
                    advance();
                }
            }
            else break;
        }
    }

    public Token nextToken() {
        skipWhitespaceAndComments();

        if (!hasNext()) {
            return new Token(TokenType.EOF, "", line, col);
        }

        int startLine = line;
        int startCol = col;
        char c = peek();

        //id e palavras reservadas:
        if (Character.isLetter(c) || c=='_') {
            int s = pos;
            while (hasNext() && (Character.isLetterOrDigit(peek()) || peek()=='_')) {
                advance();
            }
            String lexeme = source.substring(s, pos);
            TokenType type = keywords.getOrDefault(lexeme, TokenType.ID);
            return new Token(type, lexeme, startLine, startCol);
        }
        
        //literais numéricos:
        if (Character.isDigit(c)) {
            int s = pos;
            boolean isFloat = false;
            while (hasNext() && Character.isDigit(peek())) {
                advance();
            }
            if (hasNext() && peek()=='.') {
                if (pos+1<source.length() && Character.isDigit(source.charAt(pos+1))) {
                    isFloat = true;
                    advance(); 
                    while (hasNext() && Character.isDigit(peek())) {
                        advance();
                    }
                }
            }
            String lexeme = source.substring(s, pos);
            TokenType type;
            if (isFloat) {
                type = TokenType.FLOAT_LIT;
            } else {
                type = TokenType.INT_LIT;
            }
            return new Token(type, lexeme, startLine, startCol);
        }

        //strings:
        if (c=='"') {
            advance(); 
            int s = pos;
            while (hasNext() && peek()!='"' && peek()!='\n') {
                advance();
            }
            if (!hasNext() || peek()=='\n') {
                throw new LexicalError("String não terminada", startLine, startCol);
            }
            String lexeme = source.substring(s, pos);
            advance(); 
            return new Token(TokenType.STR_LIT, lexeme, startLine, startCol);
        }

        //caracteres:
        if (c=='\'') {
            advance(); 
            if (!hasNext() || peek()=='\n' || peek()=='\'') {
                throw new LexicalError("Literal de caractere inválido ou vazio", startLine, startCol);
            }
            char charValue = advance();
            if (!hasNext() || peek()!='\'') {
                throw new LexicalError("Literal de caractere não terminado", startLine, startCol);
            }
            advance(); 
            return new Token(TokenType.CHAR_LIT, String.valueOf(charValue), startLine, startCol);
        }

        //operadores e delimitadores:
        char op = advance();
        switch (op) {
            case '+': return new Token(TokenType.PLUS, "+", startLine, startCol);
            case '-': return new Token(TokenType.MINUS, "-", startLine, startCol);
            case '*': return new Token(TokenType.MULT, "*", startLine, startCol);
            case '/': return new Token(TokenType.DIV, "/", startLine, startCol);
            case '%': return new Token(TokenType.MOD, "%", startLine, startCol);
            case '=':
                if (hasNext() && peek()=='=') {
                    advance();
                    return new Token(TokenType.EQ, "==", startLine, startCol);
                }
                else return new Token(TokenType.ASSIGN, "=", startLine, startCol);
            case '!':
                if (hasNext() && peek()=='='){
                    advance();
                    return new Token(TokenType.NE, "!=", startLine, startCol);
                }
                else return new Token(TokenType.NOT, "!", startLine, startCol);
            case '>': 
                if (hasNext() && peek()=='='){
                    advance();
                    return new Token(TokenType.GE, ">=", startLine, startCol);
                }
                else return new Token(TokenType.GT, ">", startLine, startCol);
            case '<': 
                if (hasNext() && peek()=='='){
                    advance();
                    return new Token(TokenType.LE, "<=", startLine, startCol);
                }
                else return new Token(TokenType.LT, "<", startLine, startCol);
            case '&':
                if (hasNext() && peek()=='&') {
                    advance();
                    return new Token(TokenType.AND, "&&", startLine, startCol);
                }
                break;
            case '|':
                if (hasNext() && peek()=='|') {
                    advance();
                    return new Token(TokenType.OR, "||", startLine, startCol);
                }
                break;
            case '(': return new Token(TokenType.LPAREN, "(", startLine, startCol);
            case ')': return new Token(TokenType.RPAREN, ")", startLine, startCol);
            case '{': return new Token(TokenType.LBRACE, "{", startLine, startCol);
            case '}': return new Token(TokenType.RBRACE, "}", startLine, startCol);
            case '[': return new Token(TokenType.LBRACKET, "[", startLine, startCol);
            case ']': return new Token(TokenType.RBRACKET, "]", startLine, startCol);
            case ',': return new Token(TokenType.COMMA, ",", startLine, startCol);
            case '.': return new Token(TokenType.DOT, ".", startLine, startCol);
            case ';': return new Token(TokenType.SEMICOLON, ";", startLine, startCol);
        }
        throw new LexicalError("Caractere não determinado: " + op, startLine, startCol);
    }
}