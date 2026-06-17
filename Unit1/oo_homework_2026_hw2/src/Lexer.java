public class Lexer {
    private final String input;
    private int pos;
    private String curToken;
    private TokenType curType;

    public Lexer(String input) {
        this.input = preProc(input);
        this.next();
    }

    // 预处理
    private String preProc(String input) {
        String tmp = input;
        tmp = rmWhite(tmp);
        tmp = simplify(tmp);
        return tmp;
    }

    // 移除空白字符
    private String rmWhite(String s) { return s.replaceAll("\\s+", ""); }

    // 简化符号
    private String simplify(String s) {
        String tmp = s;
        while (tmp.contains("++") || tmp.contains("+-") ||
                tmp.contains("-+") || tmp.contains("--")) {
            tmp = tmp.replace("++", "+");
            tmp = tmp.replace("+-", "-");
            tmp = tmp.replace("-+", "-");
            tmp = tmp.replace("--", "+");
        }
        return tmp;
    }

    public void next() {
        if (pos >= input.length()) {
            curType = TokenType.EOF; // 表达式末尾
            return;
        }
        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            StringBuilder sb = new StringBuilder();
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                sb.append(input.charAt(pos));
                pos++;
            }
            curToken = sb.toString(); // 得到一整个数字
            curType = TokenType.NUM;
        }
        else if (Character.isLetter(c)) {
            parseIdf();
        }
        else { parseSymbol(c); }
    }

    public void skipFactor(TokenType type) {
        int rbra = 0;
        int sbra = 0;
        while (curType != TokenType.EOF) {
            if (rbra == 0 && sbra == 0 && this.checkType(type)) { break; }
            if (curType == TokenType.LSBRA) { sbra++; }
            else if (curType == TokenType.LBRA) { rbra++; }
            else if (curType == TokenType.RBRA) { rbra--; }
            else if (curType == TokenType.RSBRA) { sbra--; }
            this.next();
        }
    }

    public boolean checkType(TokenType type) { return curType == type; }

    public TokenType getType() { return curType; }

    public String peek() { return curToken; }

    // 解析标识符
    private void parseIdf() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        String idf = sb.toString();
        curToken = idf; // update curToken
        if (idf.equals("exp")) {
            curType = TokenType.EXP;
        }
        else if (idf.equals("x")) {
            curType = TokenType.VAR;
        }
        else if (idf.equals("f")) {
            curType = TokenType.FUNC;
        }
    }

    // 解析符号
    private void parseSymbol(char c) {
        curToken = String.valueOf(c); // char -> string
        switch (c) {
            case '+': {
                curType = TokenType.ADD;
                break;
            }
            case '-': {
                curType = TokenType.SUB;
                break;
            }
            case '*': {
                curType = TokenType.MUL;
                break;
            }
            case '^': {
                curType = TokenType.POW;
                break;
            }
            case '(': {
                curType = TokenType.LBRA;
                break;
            }
            case ')': {
                curType = TokenType.RBRA;
                break;
            }
            case '[': {
                curType = TokenType.LSBRA;
                break;
            }
            case ']': {
                curType = TokenType.RSBRA;
                break;
            }
            case '?': {
                curType = TokenType.QUEST;
                break;
            }
            case ':': {
                curType = TokenType.COLON;
                break;
            }
            case '=': {
                if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                    curType = TokenType.EQ;
                    pos += 2;
                    curToken = "==";
                    return;
                }
                break;
            }
            default: break;
        }
        pos++;
    }
}