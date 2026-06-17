import java.util.HashMap;
import java.util.Scanner;

public class Executer {
    private HashMap<String, Function> functions;
    private Scanner sc;

    public Executer(HashMap<String, Function> functions) {
        this.functions = functions;
        this.sc = new Scanner(System.in);
    }

    public Expr execute() { // 入口
        Expr ans;
        parseStandardFunc();
        parseRecurFunc();
        ans = getAnswer();
        return ans;
    }

    private void parseStandardFunc() { // 非递归函数
        int funcNum = Integer.parseInt(sc.nextLine());
        for (int i = 0; i < funcNum; i++) {
            String funcDef = sc.nextLine();
            funcDef = funcDef.replaceAll("\\s+", "");
            String[] func = funcDef.split("=");
            String funcName = func[0].substring(0, func[0].indexOf("(")).trim(); // 提取函数名
            String funcBody = func[1].trim(); // 提取函数体
            Lexer lexer = new Lexer(funcBody);
            Parser parser = new Parser(lexer, functions);
            Expr exprOfFunc = parser.parse();
            functions.put(funcName, new StandardFunc(exprOfFunc, "x"));
        }
    }

    private void parseRecurFunc() { // 递归函数
        int funcNum = Integer.parseInt(sc.nextLine());
        if (funcNum == 0) { return; } // no recurFunc
        String f0 = "";
        String f1 = "";
        String fn = "";
        for (int i = 0; i < 3; i++) {
            String input = sc.nextLine().replaceAll("\\s+", "");
            if (input.contains("f{0}")) { f0 = input; }
            else if (input.contains("f{1}")) { f1 = input; }
            else { fn = input; }
        }

        // get expression of f0 and f1
        String[] func0 = f0.split("=");
        String[] func1 = f1.split("=");
        Lexer lexer0 = new Lexer(func0[1]);
        Lexer lexer1 = new Lexer(func1[1]);
        Parser parser0 = new Parser(lexer0, functions);
        Parser parser1 = new Parser(lexer1, functions);
        Expr expr0 = parser0.parse();
        Expr expr1 = parser1.parse();

        String funcn = fn.split("=")[1];

        // put function
        functions.put("fr", new RecurFunc(expr0, expr1, funcn, functions));
    }

    private Expr getAnswer() { // 解析输入，递归下降
        String input = sc.nextLine();
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer, functions);
        return parser.parse();
    }
}
