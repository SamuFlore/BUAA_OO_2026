import org.junit.Test;
import static org.junit.Assert.*;

public class NetworkTest {

    @Test
    public void testQueryMutualFollowingSumCorrectness() throws Exception {
        Network network = new Network();
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 25);
        network.addUser(3, "C", 30);

        // 无互相关注
        assertEquals(0, network.queryMutualFollowingSum());

        // A <-> B 互相关注
        network.followUser(1, 2);
        network.followUser(2, 1);
        assertEquals(1, network.queryMutualFollowingSum());

        // B -> C 单向, 不计入
        network.followUser(2, 3);
        assertEquals(1, network.queryMutualFollowingSum());

        // 取消互关
        network.unfollowUser(1, 2);
        assertEquals(0, network.queryMutualFollowingSum());
    }

    @Test
    public void testQueryMutualFollowingSumPurity() throws Exception {
        Network network = new Network();
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 25);
        network.followUser(1, 2);
        network.followUser(2, 1);

        boolean beforeFollowing = network.getUser(1).isFollowing(network.getUser(2));
        boolean beforeFollower = network.getUser(1).containsFollower(network.getUser(2));
        String beforeName = network.getUser(1).getName();
        int beforeAge = network.getUser(1).getAge();

        network.queryMutualFollowingSum();

        assertTrue(beforeFollowing == network.getUser(1).isFollowing(network.getUser(2)));
        assertTrue(beforeFollower == network.getUser(1).containsFollower(network.getUser(2)));
        assertEquals(beforeName, network.getUser(1).getName());
        assertEquals(beforeAge, network.getUser(1).getAge());
    }

    @Test
    public void testQueryMutualFollowingSumIdempotent() throws Exception {
        Network network = new Network();
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 25);
        network.followUser(1, 2);
        network.followUser(2, 1);

        int first  = network.queryMutualFollowingSum();
        int second = network.queryMutualFollowingSum();
        assertEquals(first, second);
    }
}
