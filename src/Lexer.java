import java.util.*;

public class Lexer {
    private final String       source;
    private final List<Token>  tokens = new ArrayList<>();
    private int  start   = 0;
    private int  current = 0;
    private int  line    = 1;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("ধরো",    TokenType.DHORO);
        KEYWORDS.put("যদি",    TokenType.JODI);
        KEYWORDS.put("নাহলে",  TokenType.NAHOLE);
        KEYWORDS.put("দেখাও",  TokenType.PRINT);
        KEYWORDS.put("সত্য",   TokenType.TRUE);
        KEYWORDS.put("মিথ্যা", TokenType.FALSE);
    }

    public Lexer(String source) { this.source = source; }

    // Public entry point
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // scanner
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LPAREN);  break;
            case ')': addToken(TokenType.RPAREN);  break;
            case '{': addToken(TokenType.LBRACE);  break;
            case '}': addToken(TokenType.RBRACE);  break;
            case '+': addToken(TokenType.PLUS);    break;
            case '-': addToken(TokenType.MINUS);   break;
            case '*': addToken(TokenType.MUL);     break;
            case '/': addToken(TokenType.DIV);     break;
            case ';': addToken(TokenType.SEMI);    break;

            case '=':
                addToken(match('=') ? TokenType.EQ : TokenType.ASSIGN);
                break;
            case '!':
                if (match('=')) addToken(TokenType.NEQ);
                else System.err.println("Line " + line + ": Unexpected character: !");
                break;
            case '<':
                addToken(match('=') ? TokenType.LTE : TokenType.LT);
                break;
            case '>':
                addToken(match('=') ? TokenType.GTE : TokenType.GT);
                break;

            case '"': string(); break;

            case ' ':
            case '\r':
            case '\t': break;
            case '\n': line++; break;

            default:
                if (isBanglaDigit(c)) {
                    number();
                } else if (isBanglaLetter(c) || isLatinLetter(c)) {
                    identifier();
                } else {
                    System.err.println("Line " + line + ": Unexpected character: " + c + " (U+" 
                        + Integer.toHexString(c) + ")");
                }
                break;
        }
    }

    // Token builders
    private void identifier() {
        while (isBanglaLetter(peek()) || isBanglaDigit(peek()) || isLatinLetter(peek())) advance();
        String     text = source.substring(start, current);
        TokenType  type = KEYWORDS.getOrDefault(text, TokenType.ID);
        addToken(type);
    }

    private void number() {
        while (isBanglaDigit(peek())) advance();
        addToken(TokenType.NUMBER);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            System.err.println("Line " + line + ": Unterminated string.");
            return;
        }
        advance();
        
        String value = source.substring(start + 1, current - 1);
        tokens.add(new Token(TokenType.STRING, value, line));
    }

    // Helpers 
    private boolean isBanglaLetter(char c) { return c >= '\u0980' && c <= '\u09FF'; }
    private boolean isBanglaDigit(char c)  { return c >= '০'      && c <= '৯';     }
    private boolean isLatinLetter(char c)  { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'; }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char advance()  { return source.charAt(current++); }
    private char peek()     { return isAtEnd() ? '\0' : source.charAt(current); }
    private boolean isAtEnd() { return current >= source.length(); }
    private void addToken(TokenType type) {
        tokens.add(new Token(type, source.substring(start, current), line));
    }
}