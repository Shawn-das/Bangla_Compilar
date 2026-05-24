import java.util.*;

public class SemanticAnalyzer {
    private final SymbolTable symbolTable;

    public SemanticAnalyzer(SymbolTable st) { this.symbolTable = st; }

    //Public entry point 
    public void analyze(List<ASTNode> nodes) {
        for (ASTNode node : nodes) evaluate(node);
    }

    // Evaluator 
    // Returns a boxed value (Integer, String, Boolean) and stores types.
    Object evaluate(ASTNode node) {

        if (node instanceof NumberNode) return ((NumberNode) node).value;
        if (node instanceof StringNode) return ((StringNode) node).value;
        if (node instanceof BoolNode)   return ((BoolNode)   node).value;

        if (node instanceof VarNode) {
            String name = ((VarNode) node).name;
            if (!symbolTable.contains(name))
                throw new RuntimeException(
                    "Semantic Error: Variable '" + name + "' used before declaration.");
            return symbolTable.get(name).value;
        }

        if (node instanceof BinOpNode) {
            BinOpNode b  = (BinOpNode) node;
            Object    lv = evaluate(b.left);
            Object    rv = evaluate(b.right);
            return applyOp(b.operator, lv, rv);
        }

        if (node instanceof AssignNode) {
            AssignNode a     = (AssignNode) node;
            Object     value = evaluate(a.expr);
            SymbolTable.VarType type = inferType(value);
            symbolTable.set(a.name, type, value);
            return value;
        }

        if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            Object cond   = evaluate(ifNode.condition);
            if (!(cond instanceof Boolean))
                throw new RuntimeException(
                    "Type Error: 'যদি' condition must be boolean (সত্য/মিথ্যা or comparison).");
            if ((Boolean) cond) {
                for (ASTNode s : ifNode.thenBranch) evaluate(s);
            } else if (ifNode.elseBranch != null) {
                for (ASTNode s : ifNode.elseBranch) evaluate(s);
            }
            return null;
        }

        if (node instanceof PrintNode) {
            Object val = evaluate(((PrintNode) node).expr);
            // Displayed in code generator; we just validate here
            return val;
        }

        return null;
    }

    // Operator semantics with type safety 
    private Object applyOp(String op, Object lv, Object rv) {
        // Comparison operators work on ints, strings, and booleans
        switch (op) {
            case "==": return lv.equals(rv);
            case "!=": return !lv.equals(rv);
        }

        // Arithmetic: INT only
        if (lv instanceof Integer && rv instanceof Integer) {
            int l = (int) lv, r = (int) rv;
            switch (op) {
                case "+":  return l + r;
                case "-":  return l - r;
                case "*":  return l * r;
                case "/":
                    if (r == 0) throw new RuntimeException("Semantic Error: Division by zero.");
                    return l / r;
                case "<":  return l < r;
                case ">":  return l > r;
                case "<=": return l <= r;
                case ">=": return l >= r;
            }
        }

        // String concatenation with +
        if (lv instanceof String && rv instanceof String && op.equals("+"))
            return (String) lv + (String) rv;

        throw new RuntimeException(
            "Type Error: Operator '" + op + "' cannot be applied to '"
            + typeName(lv) + "' and '" + typeName(rv) + "'.");
    }

    private SymbolTable.VarType inferType(Object v) {
        if (v instanceof Integer) return SymbolTable.VarType.INT;
        if (v instanceof String)  return SymbolTable.VarType.STRING;
        if (v instanceof Boolean) return SymbolTable.VarType.BOOL;
        throw new RuntimeException("Unknown type for value: " + v);
    }

    private String typeName(Object v) {
        if (v instanceof Integer) return "INT";
        if (v instanceof String)  return "STRING";
        if (v instanceof Boolean) return "BOOL";
        return "UNKNOWN";
    }

    //  Bangla <-> English digit conversion
    public static String convertEnglishToBangla(int number) {
        StringBuilder sb  = new StringBuilder();
        String        eng = String.valueOf(number);
        for (char c : eng.toCharArray()) {
            if (c == '-') sb.append('-');
            else          sb.append((char) (c - '0' + '০'));
        }
        return sb.toString();
    }

    public static int convertBanglaToEnglish(String bNum) {
        StringBuilder sb = new StringBuilder();
        for (char c : bNum.toCharArray()) sb.append((char) (c - '০' + '0'));
        return Integer.parseInt(sb.toString());
    }
}