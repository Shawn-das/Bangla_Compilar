import java.util.List;

// Base 
public abstract class ASTNode {}

// Literals 
class NumberNode extends ASTNode {
    int value;
    NumberNode(int value) { this.value = value; }
}

class StringNode extends ASTNode {
    String value;
    StringNode(String value) { this.value = value; }
}

class BoolNode extends ASTNode {
    boolean value;
    BoolNode(boolean value) { this.value = value; }
}

// Identifiers 
class VarNode extends ASTNode {
    String name;
    VarNode(String name) { this.name = name; }
}

// Expressions 
class BinOpNode extends ASTNode {
    ASTNode left;
    String  operator;
    ASTNode right;
    BinOpNode(ASTNode left, String operator, ASTNode right) {
        this.left = left; this.operator = operator; this.right = right;
    }
}

// Statements 
class AssignNode extends ASTNode {
    String  name;
    ASTNode expr;
    AssignNode(String name, ASTNode expr) { this.name = name; this.expr = expr; }
}

class IfNode extends ASTNode {
    ASTNode            condition;
    List<ASTNode>      thenBranch;
    List<ASTNode>      elseBranch;   // may be null
    IfNode(ASTNode condition, List<ASTNode> thenBranch, List<ASTNode> elseBranch) {
        this.condition  = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}

class PrintNode extends ASTNode {
    ASTNode expr;
    PrintNode(ASTNode expr) { this.expr = expr; }
}