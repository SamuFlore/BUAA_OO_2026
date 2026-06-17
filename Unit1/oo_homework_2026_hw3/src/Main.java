import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        Expr ans;
        HashMap<String, Function> functions = new HashMap<>();
        Executer world = new Executer(functions);
        ans = world.execute(/* me */);
        System.out.println(ans.toString());
    }
}


