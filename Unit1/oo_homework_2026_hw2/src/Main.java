import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int funcNum;
        HashMap<String, Expr> functions = new HashMap<>();
        Scanner sc = new Scanner(System.in);
        funcNum = Integer.parseInt(sc.nextLine());
        String input = "";
        for (int i = 0; i < funcNum; i++) {
            String funcDef = sc.nextLine();
            funcDef = funcDef.replaceAll("\\s+", "");
            String[] func = funcDef.split("=");
            String funcName = func[0].substring(0, func[0].indexOf("(")).trim(); // 提取函数名
            String funcBody = func[1].trim(); // 提取函数体
            Lexer lexer = new Lexer(funcBody);
            Parser parser = new Parser(lexer, functions);
            Expr exprOfFunc = parser.parse();
            functions.put(funcName, exprOfFunc);
        }

        input = sc.nextLine();
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer, functions);
        Expr ans = parser.parse();
        System.out.println(ans.toString());
    }
}
