import java.util.*;
import java.io.PrintWriter;

public class SymbolTable {
    public enum VarType { INT, STRING, BOOL }

    public static class Entry {
        public final VarType type;
        public final Object  value;
        Entry(VarType type, Object value) { this.type = type; this.value = value; }
    }

    
    private final Map<String, Entry> table = new LinkedHashMap<>();

    public void set(String name, VarType type, Object value) {
        // Type-change check: a variable's type is locked after first declaration
        if (table.containsKey(name)) {
            VarType existing = table.get(name).type;
            if (existing != type) {
                throw new RuntimeException(
                    "Type Error: Cannot assign " + type + " value to variable '" + name
                    + "' which was declared as " + existing + ".");
            }
        }
        table.put(name, new Entry(type, value));
    }

    public Entry get(String name)         { return table.get(name); }
    public boolean contains(String name)  { return table.containsKey(name); }

    public void displayToFile(PrintWriter out) {
        out.println(String.format("%-15s %-8s %s", "Variable", "Type", "Value"));
        out.println("-".repeat(40));
        if (table.isEmpty()) {
            out.println("(No variables defined)");
        } else {
            table.forEach((k, e) -> {
                String display = (e.type == SymbolTable.VarType.INT)
                    ? SemanticAnalyzer.convertEnglishToBangla((int) e.value)
                    : String.valueOf(e.value);
                out.println(String.format("%-15s %-8s %s", k, e.type, display));
            });
        }
    }
}