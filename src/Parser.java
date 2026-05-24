import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private final List<String> errors = new ArrayList<>();
    private int current = 0;

    Parser(List<Token> tokens) { this.tokens = tokens; }
    List<String> getErrors()   { return errors; }

    List<ASTNode> parse() {
        List<ASTNode> nodes = new ArrayList<>();
        while (!isAtEnd()) {
            if (match(TokenType.SEMI)) continue;
            ASTNode s = statement();
            if (s != null) nodes.add(s);
        }
        return nodes;
    }

    private ASTNode statement() {
        try {
            if (match(TokenType.DHORO)) return declaration();
            if (match(TokenType.JODI))  return ifStatement();
            if (match(TokenType.PRINT)) { ASTNode e = expression(); eat(TokenType.SEMI, "';'"); return new PrintNode(e); }
            throw new Err("Expected statement at line " + peek().line);
        } catch (Err e) {
            errors.add(e.getMessage());
            synchronize();
            return null;
        }
    }

    private AssignNode declaration() {
        Token name = eat(TokenType.ID, "variable name");
        eat(TokenType.ASSIGN, "'='");
        ASTNode expr = expression();
        eat(TokenType.SEMI, "';'");
        return new AssignNode(name.lexeme, expr);
    }

    private IfNode ifStatement() {
        eat(TokenType.LPAREN, "'('");
        ASTNode cond = expression();
        eat(TokenType.RPAREN, "')'");
        eat(TokenType.LBRACE, "'{'");
        List<ASTNode> then = block();
        List<ASTNode> els  = null;
        if (match(TokenType.NAHOLE)) { eat(TokenType.LBRACE, "'{'"); els = block(); }
        return new IfNode(cond, then, els);
    }

    private List<ASTNode> block() {
        List<ASTNode> nodes = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            if (match(TokenType.SEMI)) continue;
            ASTNode s = statement();
            if (s != null) nodes.add(s);
        }
        eat(TokenType.RBRACE, "'}'");
        return nodes;
    }

    private ASTNode expression() {
        ASTNode n = addition();
        while (match(TokenType.EQ,TokenType.NEQ,TokenType.LT,TokenType.GT,TokenType.LTE,TokenType.GTE))
            n = new BinOpNode(n, previous().lexeme, addition());
        return n;
    }

    private ASTNode addition() {
        ASTNode n = term();
        while (match(TokenType.PLUS, TokenType.MINUS))
            n = new BinOpNode(n, previous().lexeme, term());
        return n;
    }

    private ASTNode term() {
        ASTNode n = primary();
        while (match(TokenType.MUL, TokenType.DIV))
            n = new BinOpNode(n, previous().lexeme, primary());
        return n;
    }

    private ASTNode primary() {
        if (match(TokenType.NUMBER)) return new NumberNode(SemanticAnalyzer.convertBanglaToEnglish(previous().lexeme));
        if (match(TokenType.STRING)) return new StringNode(previous().lexeme);
        if (match(TokenType.TRUE))   return new BoolNode(true);
        if (match(TokenType.FALSE))  return new BoolNode(false);
        if (match(TokenType.ID))     return new VarNode(previous().lexeme);
        if (match(TokenType.LPAREN)) { ASTNode e = expression(); eat(TokenType.RPAREN, "')'"); return e; }
        throw new Err("Expected expression at line " + peek().line);
    }

    private void synchronize() {
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMI || previous().type == TokenType.RBRACE) return;
            TokenType t = peek().type;
            if (t==TokenType.DHORO||t==TokenType.JODI||t==TokenType.PRINT||t==TokenType.RBRACE) return;
            advance();
        }
    }

    private Token eat(TokenType t, String expected) {
        if (check(t)) return advance();
        throw new Err("Expected " + expected + " (got '" + peek().lexeme + "' at line " + peek().line + ")");
    }
    private boolean match(TokenType... types) { for (TokenType t:types) if (check(t)){advance();return true;} return false; }
    private boolean check(TokenType t)  { return !isAtEnd() && peek().type == t; }
    private Token   advance()           { if (!isAtEnd()) current++; return previous(); }
    private boolean isAtEnd()           { return peek().type == TokenType.EOF; }
    private Token   peek()              { return tokens.get(current); }
    private Token   previous()          { return tokens.get(current - 1); }

    static class Err extends RuntimeException { Err(String m) { super(m); } }
}