public class Lexer {
    private final String input;
    private int pos;
    private String curToken;
    private TokenType curType;

    public Lexer(String input) {
        this.input = rmWhite(input);
        this.next();
    }

    // 移除空白字符
    private String rmWhite(String s) { return s.replaceAll("\\s+", ""); }

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
        else if (c == 'x') {
            curToken = "x";
            curType = TokenType.VAR; // 变量
            pos++;
        }
        else {
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
                default: break;
            }
            pos++;
        }
    }

    public boolean checkType(TokenType type) { return curType == type; }

    public TokenType getType() { return curType; }

    public String peek() { return curToken; }
}