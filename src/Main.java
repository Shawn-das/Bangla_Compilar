import java.util.*;
import java.nio.file.*;
import java.io.*;

public class Main {

    public static void main(String[] args) throws Exception {
        String inputPath  = (args.length > 0) ? args[0] : "input.txt";
        String reportPath = "output.txt";
        String pyPath     = "output.py";

        // ── Read source ──────────────────────────────────────────────────────
        String source;
        try {
            source = Files.readString(Paths.get(inputPath));
        } catch (IOException e) {
            System.err.println("Error: Cannot read '" + inputPath + "'");
            return;
        }

        // ── Open report writer ───────────────────────────────────────────────
        try (PrintWriter report = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(reportPath), "UTF-8"))) {

            // ── STEP 1: LEXICAL ANALYSIS ─────────────────────────────────────
            Lexer      lexer  = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();

            report.println("=== STEP 1: LEXICAL ANALYSIS (TOKENS) ===");
            for (Token t : tokens) report.println(t);

            // ── STEP 2: SYNTAX ANALYSIS ──────────────────────────────────────
            report.println("\n=== STEP 2: SYNTAX ANALYSIS (AST) ===");
            Parser       parser   = new Parser(tokens);
            List<ASTNode> astNodes = parser.parse();

            // Report parse errors (recovered)
            List<String> parseErrors = parser.getErrors();
            if (!parseErrors.isEmpty()) {
                report.println("[Syntax Errors Recovered]:");
                for (String err : parseErrors) report.println("  !! " + err);
                report.println();
            }

            for (ASTNode node : astNodes) writeTree(node, 0, report);

            // ── STEP 3: SEMANTIC ANALYSIS ────────────────────────────────────
            report.println("\n=== STEP 3: SEMANTIC ANALYSIS & SYMBOL TABLE ===");
            SymbolTable      st       = new SymbolTable();
            SemanticAnalyzer analyzer = new SemanticAnalyzer(st);

            boolean semanticOk = true;
            try {
                analyzer.analyze(astNodes);
                report.println("[Status]: Type checking passed.");
                report.println("[Status]: Scope check passed.");
            } catch (RuntimeException e) {
                semanticOk = false;
                report.println("[SEMANTIC ERROR]: " + e.getMessage());
            }

            report.println("-".repeat(40));
            st.displayToFile(report);

            // ── STEP 4: CODE GENERATION ──────────────────────────────────────
            report.println("\n=== STEP 4: CODE GENERATION ===");
            if (!semanticOk) {
                report.println("[Skipped due to semantic errors]");
                System.err.println("Semantic error(s) found. Check output.txt");
            } else {
                CodeGenerator gen    = new CodeGenerator();
                String        pyCode = gen.generate(astNodes);

                // Write Python file
                try (PrintWriter pyOut = new PrintWriter(
                        new OutputStreamWriter(new FileOutputStream(pyPath), "UTF-8"))) {
                    pyOut.print(pyCode);
                }

                report.println("[Status]: Python file generated successfully: " + pyPath);
                report.println("\n--- Generated Python Code ---");
                report.println(pyCode);
                report.println("-----------------------------");

                System.out.println("Compilation successful!");
                System.out.println("  Report  -> " + reportPath);
                System.out.println("  Python  -> " + pyPath);
                System.out.println();
                System.out.println("Run the program with:  python3 " + pyPath);
            }
        }
    }

    // ── AST pretty-printer ───────────────────────────────────────────────────
    static void writeTree(ASTNode node, int indent, PrintWriter out) {
        if (node == null) return;
        String p = "  ".repeat(indent);

        if (node instanceof AssignNode) {
            AssignNode a = (AssignNode) node;
            out.println(p + "Assignment: " + a.name);
            writeTree(a.expr, indent + 1, out);
        } else if (node instanceof IfNode) {
            IfNode ifn = (IfNode) node;
            out.println(p + "If:");
            out.println(p + "  Condition:");
            writeTree(ifn.condition, indent + 2, out);
            out.println(p + "  Then:");
            for (ASTNode s : ifn.thenBranch) writeTree(s, indent + 2, out);
            if (ifn.elseBranch != null) {
                out.println(p + "  Else:");
                for (ASTNode s : ifn.elseBranch) writeTree(s, indent + 2, out);
            }
        } else if (node instanceof PrintNode) {
            out.println(p + "Print:");
            writeTree(((PrintNode) node).expr, indent + 1, out);
        } else if (node instanceof BinOpNode) {
            BinOpNode b = (BinOpNode) node;
            out.println(p + "BinaryOp: " + b.operator);
            writeTree(b.left,  indent + 1, out);
            writeTree(b.right, indent + 1, out);
        } else if (node instanceof VarNode) {
            out.println(p + "Variable: " + ((VarNode) node).name);
        } else if (node instanceof NumberNode) {
            out.println(p + "Integer: " + SemanticAnalyzer.convertEnglishToBangla(((NumberNode) node).value));
        } else if (node instanceof StringNode) {
            out.println(p + "String: \"" + ((StringNode) node).value + "\"");
        } else if (node instanceof BoolNode) {
            out.println(p + "Bool: " + (((BoolNode) node).value ? "সত্য" : "মিথ্যা"));
        }
    }
}