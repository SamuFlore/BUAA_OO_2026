import java.util.HashMap;

public class RecurFunc implements Function {
    private Expr[] exprs = new Expr[6]; // 0,1,2,3,4,5
    private String recurDef;
    private HashMap<String, Function> functions;

    // f{n}(x) = coeff1 * f{n-1}(x) + coeff2 * f{n-2}(x)
    public RecurFunc(Expr f0, Expr f1, String recurDef, HashMap<String, Function> functions) {
        exprs[0] = f0;
        exprs[1] = f1;
        this.recurDef = recurDef;
        this.functions = functions;
    }

    // 如果要用递推函数，再打表
    private void generate(int index) {
        for (int i = 2; i <= index; i++) {
            if (exprs[i] != null) { continue; } // 算过的跳过
            HashMap<String, Function> funcClone = new HashMap<>(functions);
            funcClone.put("f{n-1}", new StandardFunc(exprs[i - 1], "x"));
            funcClone.put("f{n-2}", new StandardFunc(exprs[i - 2], "x"));
            Lexer tmpLexer = new Lexer(recurDef);
            Parser tmpParser = new Parser(tmpLexer, funcClone);
            exprs[i] = tmpParser.parse();
        }
    }

    @Override
    public Expr expand(Expr arg, Integer index) {
        if (exprs[index] == null) { generate(index); }
        return exprs[index].replace(arg);
    }
}
