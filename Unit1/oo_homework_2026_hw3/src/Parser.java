import java.math.BigInteger;
import java.util.HashMap;

public class Parser {
    private Lexer lexer;
    private HashMap<String, Function> functions;

    public Parser(Lexer lexer, HashMap<String, Function> functions) {
        this.lexer = lexer;
        this.functions = functions;
    }

    // 入口
    public Expr parse() {
        return parseExpr();
    }

    // Expr = Term + Term + ...
    private Expr parseExpr() {
        Expr res = new Expr();
        int sign = 1;
        if (lexer.checkType(TokenType.ADD)) { lexer.next(); }
        else if (lexer.checkType(TokenType.SUB)) {
            sign = -1;
            lexer.next();
        }

        // 解析第一项
        res = parseTerm();
        if (sign == -1) {
            res = Expr.createConst(BigInteger.ZERO).exprSub(res);
        }

        while (lexer.checkType(TokenType.ADD) ||  lexer.checkType(TokenType.SUB)) {
            TokenType tokenType = lexer.getType();
            lexer.next();
            if (tokenType == TokenType.ADD) { res = res.exprAdd(parseTerm()); }
            else if (tokenType == TokenType.SUB) { res = res.exprSub(parseTerm()); }
        }
        return res;
    }

    // Term = Factor * Factor * ...
    private Expr parseTerm() {
        Expr res = new Expr();
        int sign = 1;
        if (lexer.checkType(TokenType.ADD)) { lexer.next(); }
        else if (lexer.checkType(TokenType.SUB)) {
            sign = -1;
            lexer.next();
        }

        res = parseFactor();
        if (sign == -1) {
            res = Expr.createConst(BigInteger.ZERO).exprSub(res);
        }

        while (lexer.checkType(TokenType.MUL)) {
            lexer.next();
            res = res.exprMul(parseFactor());
        }
        return res;
    }

    // NUM, EXP(), VAR, FUNC, (EXPR), [CHOICE], DX/DY/GRAD
    private Expr parseFactor() {
        BigInteger sign = BigInteger.ONE;
        TokenType nextType = lexer.getType();
        if (lexer.checkType(TokenType.ADD) ||
                lexer.checkType(TokenType.SUB) ||
                lexer.checkType(TokenType.NUM)) {
            if (nextType == TokenType.SUB) {
                sign = BigInteger.valueOf(-1);
                lexer.next();
            }
            else if (nextType == TokenType.ADD) { lexer.next(); }
            if (lexer.checkType(TokenType.NUM)) {
                BigInteger num = new BigInteger(lexer.peek());
                lexer.next();
                return Expr.createConst(num).exprMul(Expr.createConst(sign));
            }
        }
        else if (lexer.checkType(TokenType.VAR) || lexer.checkType(TokenType.VARY)) { // X^EXPONENT
            return parseVarFactor();
        }
        else if (lexer.checkType(TokenType.LBRA)) { // (EXPR)^EXPONENT
            return parseExprFactor();
        }
        else if (lexer.checkType(TokenType.EXP)) { // EXP(EXPCONTENT)^EXPONENT
            return parseExpFactor();
        }
        else if (lexer.checkType(TokenType.LSBRA)) { // [(FACTOR1 == FACTOR2) ? FACTOR3 : FACTOR4]
            return parseChoiceFactor();
        }
        else if (lexer.checkType(TokenType.FUNC)) { // FUNC(ARG)
            return parseFuncFactor();
        }
        else if (lexer.checkType(TokenType.DX)) { // DX(EXPR)
            return parseDx();
        }
        else if (lexer.checkType(TokenType.DY)) { // DY(EXPR)
            return parseDy();
        }
        else if (lexer.checkType(TokenType.GRAD)) {
            return parseGrad();
        }
        return null;
    }

    private Expr parseVarFactor() {
        Expr ans = new Expr();
        if (lexer.checkType(TokenType.VAR)) {
            lexer.next(); // skip x
            int exponent = parseExponet();
            // return x^exponent*y^0
            ans = Expr.createVar(BigInteger.valueOf(exponent), BigInteger.ZERO);
        }
        else if (lexer.checkType(TokenType.VARY)) {
            lexer.next(); // skip y
            int exponentY = parseExponet();
            // return x^0*y^exponentY
            ans = Expr.createVar(BigInteger.ZERO, BigInteger.valueOf(exponentY));
        }
        return ans;
    }

    private Expr parseExprFactor() {
        lexer.next(); // 略过 (
        Expr expression =  parseExpr();
        lexer.next(); // 略过 )
        int exponent = parseExponet();
        return expression.exprPow(exponent);
    }

    private Expr parseExpFactor() {
        lexer.next(); // 略过 exp
        lexer.next(); // 略过 (
        Expr expContent =  parseFactor(); // EXP(FACTOR) !!!
        lexer.next(); // 略过 )
        Expr expTemp = Expr.createExp(expContent);
        int exponent = parseExponet();
        return expTemp.exprPow(exponent);
    }

    private Expr parseChoiceFactor() {
        lexer.next(); // [
        lexer.next(); // (
        Expr factor1 = parseFactor();
        lexer.next(); // ==
        Expr factor2 = parseFactor();
        lexer.next(); // )
        lexer.next(); // ?
        boolean isEqual = factor1.equals(factor2);
        Expr res = new Expr();
        if (isEqual) {
            res = parseFactor();
            lexer.skipFactor(TokenType.RSBRA);
            lexer.next(); // ]
        }
        else {
            lexer.skipFactor(TokenType.COLON);
            lexer.next(); // :
            res = parseFactor();
            lexer.next();
        }
        return res;
    }

    private Expr parseFuncFactor() {
        String funcName = lexer.peek();
        lexer.next(); // f or f{n-1} or f{n-2}
        Integer index = null;
        if (lexer.checkType(TokenType.LCBRA)) { // {
            funcName = "fr"; // avoid the same name!
            lexer.next(); // {
            index = Integer.parseInt(lexer.peek());
            lexer.next(); // index
            lexer.next(); // }
        }
        lexer.next(); // (
        Expr arg = parseFactor(); // FUNC(FACTOR)
        lexer.next(); // )
        Function func = functions.get(funcName);
        return func.expand(arg, index);
    }

    private Expr parseDx() {
        lexer.next(); // dx
        lexer.next(); // (
        Expr expression = parseExpr();
        lexer.next(); // )
        return expression.exprDeriv("x");
    }

    private Expr parseDy() {
        lexer.next(); // dy
        lexer.next(); // (
        Expr expression = parseExpr();
        lexer.next(); // )
        return expression.exprDeriv("y");
    }

    private Expr parseGrad() {
        lexer.next(); // grad
        lexer.next(); // (
        Expr expression = parseExpr();
        lexer.next(); // )
        // grad = dx + dy
        return expression.exprDeriv("x").exprAdd(expression.exprDeriv("y"));
    }

    // VAR, VARY, EXPR, EXP统一解析指数
    private int parseExponet() {
        int sign = 1;
        int exp = 1;
        if (lexer.checkType(TokenType.POW)) {
            lexer.next(); // 略过 ^
            if (lexer.checkType(TokenType.ADD)) {
                lexer.next();
            }
            else if (lexer.checkType(TokenType.SUB)) {
                sign = -1;
                lexer.next();
            }
            exp = Integer.parseInt(lexer.peek());
            lexer.next();
        }
        return exp * sign;
    }

}
