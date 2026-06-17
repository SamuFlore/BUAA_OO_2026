public enum TokenType {
    ADD, SUB, MUL, POW, // + - * ^
    LBRA, RBRA, // ( )
    VAR, VARY, // x, y
    NUM, // Big Integer
    LSBRA, RSBRA, // [ ]
    LCBRA, RCBRA, // { }
    EQ, // ==
    EXP, // exp()
    FUNC, // f()
    QUEST, COLON, // ? :
    DX, DY, GRAD, // dx, dy, grad
    EOF
}