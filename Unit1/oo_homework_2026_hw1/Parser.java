import java.math.BigInteger;

public class Parser {
    private Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    // 顶层入口
    public Poly parse() {
        return parseExpr();
    }

    // 解析表达式
    private Poly parseExpr() {
        Poly result = new Poly();

        // 处理表达式开头的符号
        int sign = 1;
        if (lexer.checkType(TokenType.ADD)) {
            lexer.next();
        }
        else if (lexer.checkType(TokenType.SUB)) {
            sign = -1;
            lexer.next();
        }

        // 解析第一个项
        result = parseTerm();
        if (sign == -1) {
            result = new Poly().sub(result); // 如果为负，则为 0 - 第一项
        }

        // 循环处理后面的所有项
        while (lexer.checkType(TokenType.ADD) || lexer.checkType(TokenType.SUB)) {
            boolean isAdd = lexer.checkType(TokenType.ADD); // 如果下一项是正，就加，否则减
            lexer.next();
            Poly nextTerm = parseTerm();
            if (isAdd) {
                result = result.add(nextTerm);
            }
            else {
                result = result.sub(nextTerm);
            }
        }
        return result;
    }

    // 解析项
    private Poly parseTerm() {
        int sign = 1;
        if (lexer.checkType(TokenType.ADD)) {
            lexer.next();
        }
        else if (lexer.checkType(TokenType.SUB)) {
            sign = -1;
            lexer.next();
        }

        // 处理第一个因子
        Poly result = parseFactor();
        if (sign == -1) {
            result = new Poly().sub(result); // 同上
        }

        // 循环解析因子（因子 * 因子）
        while (lexer.checkType(TokenType.MUL)) {
            lexer.next();
            Poly newFactor = parseFactor();
            result = result.mul(newFactor);
        }
        return result;
    }

    // 解析因子
    private Poly parseFactor() {
        Poly result = new Poly();
        // 常数因子：包含一个带符号整数，如：233 。(ax^0)，-233
        TokenType nextType = lexer.getType();
        if (nextType == TokenType.ADD || nextType == TokenType.SUB || nextType == TokenType.NUM) {
            BigInteger sign = BigInteger.ONE; // 符号
            if (lexer.checkType(TokenType.ADD)) {
                lexer.next();
            }
            else if (lexer.checkType(TokenType.SUB)) {
                sign = BigInteger.valueOf(-1);
                lexer.next();
            }
            if (lexer.checkType(TokenType.NUM)) {
                BigInteger coeff = new BigInteger(lexer.peek());
                result.addTerm(BigInteger.ZERO, coeff.multiply(sign));
                lexer.next();
            }
        }
        // 变量因子：由自变量 x，指数符号 ^ 和指数组成，指数为一个非负带符号整数，如：x ^ +2,x ^ 02,x ^ 2 。
        // 若指数为 1 可以省略。
        // 必以 VAR 开头
        else if (lexer.checkType(TokenType.VAR)) {
            result.addTerm(BigInteger.ONE, BigInteger.ONE); // 1*x^1, 若有指数，后面统一解析
            lexer.next();
        }
        // 表达式因子，包含在括号内，递归调用 parseExpr
        else if (lexer.checkType(TokenType.LBRA)) {
            lexer.next(); // 跳过 "("
            result = parseExpr();
            lexer.next(); // 跳过 ")"
        }

        // 统一解析指数
        if (lexer.checkType(TokenType.POW)) {
            lexer.next(); // 跳过 "^"
            int exp = parseExp();
            result = result.pow(exp);
        }
        return result;
    }

    private int parseExp() {
        int sign = 1;
        if (lexer.checkType(TokenType.ADD)) {
            lexer.next();
        }
        else if (lexer.checkType(TokenType.SUB)) {
            sign = -1;
            lexer.next();
        }
        int exp = Integer.parseInt(lexer.peek());
        lexer.next();
        return exp * sign;
    }
}
