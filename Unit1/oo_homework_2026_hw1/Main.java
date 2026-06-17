import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (sc.hasNextLine()) {
            String input = sc.nextLine();
            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer);
            Poly answer = parser.parse();
            System.out.println(answer.toString());
        }
        sc.close();
    }
}
