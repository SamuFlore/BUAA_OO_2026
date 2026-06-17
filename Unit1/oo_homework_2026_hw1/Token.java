public class Token {
    private final TokenType type;
    private final String value;

    public Token(TokenType tokenType, String value) {
        this.type = tokenType;
        this.value = value;
    }

    public TokenType getType() { return this.type; }

    public String getValue() { return this.value; }
}
