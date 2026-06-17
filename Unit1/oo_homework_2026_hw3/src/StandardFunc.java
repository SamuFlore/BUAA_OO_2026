public class StandardFunc implements Function {
    private Expr body;
    private String param; // 形参

    public StandardFunc(Expr body, String param) {
        this.body = body;
        this.param = param;
    }

    @Override
    public Expr expand(Expr arg, Integer index) {
        return body.replace(arg); // 参数代入形参
    }
}
