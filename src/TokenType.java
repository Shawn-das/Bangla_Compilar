public enum TokenType {
    // Keywords
    DHORO,      // ধরো  - variable declaration
    JODI,       // যদি  - if
    NAHOLE,     // নাহলে - else
    PRINT,      // দেখাও - print

    // Literals
    ID,         // Bangla identifiers
    NUMBER,     // Bangla digits ০-৯
    STRING,     // "..." string literals
    TRUE,       // সত্য
    FALSE,      // মিথ্যা

    // Arithmetic operators
    PLUS,       // +
    MINUS,      // -
    MUL,        // *
    DIV,        // /

    // Comparison operators
    EQ,         // ==
    NEQ,        // !=
    LT,         // <
    GT,         // >
    LTE,        // <=
    GTE,        // >=

    // Symbols
    ASSIGN,     // =
    LPAREN,     // (
    RPAREN,     // )
    LBRACE,     // {
    RBRACE,     // }
    SEMI,       // ;

    EOF
}