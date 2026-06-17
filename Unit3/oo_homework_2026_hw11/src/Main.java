import com.oocourse.spec3.main.Runner;

public class Main {
    public static void main(String[] args) throws Exception {
        Runner runner = new Runner(User.class, Network.class, Video.class);
        runner.run();
    }
}