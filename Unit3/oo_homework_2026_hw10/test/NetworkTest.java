import com.oocourse.spec2.exceptions.VideoIdNotFoundException;
import com.oocourse.spec2.main.VideoInterface;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class NetworkTest {
    Network network1;
    Network network2;

    // ==================== QueryMutualFollowingSum ====================

    @Test
    public void testQueryMutualFollowingSumCorrectness() throws Exception {
        Network network = new Network();
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 25);
        network.addUser(3, "C", 30);

        assertEquals(0, network.queryMutualFollowingSum());

        network.followUser(1, 2);
        network.followUser(2, 1);
        assertEquals(1, network.queryMutualFollowingSum());

        network.followUser(2, 3);
        assertEquals(1, network.queryMutualFollowingSum());

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

    // ==================== CleanSpamComments ====================

    @Test
    public void testCleanSpamComments() throws Exception {
        network1 = new Network();
        network2 = new Network();
        genVideos(network1);
        genVideos(network2);

        // 初始时无评论
        assertArrayEquals(new int[] {0, 0}, network1.cleanSpamComments(201, "empty"));
        checkVideoSame(201);
        checkVideoSame(203); // 203 对照
        checkVideoSame(202);

        // 常规
        // 视频 201 上添加 4 条评论, 视频 202 上添加 1 条
        network1.sendComment(20, 201, 11, "ababab");
        network2.sendComment(20, 201, 11, "ababab");
        network1.sendComment(30, 201, 22, "xyz");
        network2.sendComment(30, 201, 22, "xyz");
        network1.sendComment(20, 201, 33, "xababxababab");
        network2.sendComment(20, 201, 33, "xababxababab");
        network1.sendComment(30, 201, 44, "nothing");
        network2.sendComment(30, 201, 44, "nothing");
        network1.sendComment(20, 201, 55, "xababxa");
        network2.sendComment(20, 201, 55, "xababxa");
        network1.sendComment(20, 202, 66, "abab");
        network2.sendComment(20, 202, 66, "abab");

        network1.sendComment(40, 203, 66, "abababababababababab");
        network2.sendComment(40, 203, 66, "abababababababababab");
        
        assertArrayEquals(new int[] {3, 5}, network1.cleanSpamComments(201, "ab"));
        checkSideEffect(201);
        checkVideoSame(203); // 203 对照
        checkVideoSame(202);

        Video v201 = (Video) network1.getVideo(201);
        int[] ids = v201.getCommentIds();
        String[] contents = v201.getCommentContents();
        assertEquals(ids.length, contents.length);
        assertEquals(2, ids.length);
        boolean has22 = false;
        boolean has44 = false;
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == 22) {
                assertEquals("xyz", contents[i]);
                has22 = true;
            } else if (ids[i] == 44) {
                assertEquals("nothing", contents[i]);
                has44 = true;
            } else {
                fail("Unexpected comment id: " + ids[i]);
            }
        }
        assertTrue(has22);
        assertTrue(has44);

        network2.cleanSpamComments(201, "ab"); // 同步对照组

        // 空串 keyword
        network1.sendComment(20, 201, 11, "ok");
        network2.sendComment(20, 201, 11, "ok");
        network1.sendComment(30, 201, 66, "test case");
        network2.sendComment(30, 201, 66, "test case");
        network1.sendComment(20, 202, 33, "extra");
        network2.sendComment(20, 202, 33, "extra");

        // 剩余 4 条全部含空串: "xyz"→4, "nothing"→8, "ok"→3, "test case"→10, maxCount=10
        assertArrayEquals(new int[] {4, 10}, network1.cleanSpamComments(201, ""));
        checkSideEffect(201);
        checkVideoSame(203);
        checkVideoSame(202);

        v201 = (Video) network1.getVideo(201);
        ids = v201.getCommentIds();
        contents = v201.getCommentContents();
        assertEquals(ids.length, contents.length);
        assertEquals(0, ids.length);

        network2.cleanSpamComments(201, ""); // 同步对照组

        // 无匹配 keyword
        network1.sendComment(20, 201, 11, "hello");
        network2.sendComment(20, 201, 11, "hello");

        assertArrayEquals(new int[] {0, 0}, network1.cleanSpamComments(201, "zz"));
        checkVideoSame(201);
        checkVideoSame(203);
        checkVideoSame(202);

        v201 = (Video) network1.getVideo(201);
        ids = v201.getCommentIds();
        contents = v201.getCommentContents();
        assertEquals(ids.length, contents.length);
        assertEquals(1, ids.length);
        assertEquals(11, ids[0]);
        assertEquals("hello", contents[0]);

        network2.cleanSpamComments(201, "zz"); // 同步对照组

        // VideoIdNotFoundException
        try {
            network1.cleanSpamComments(9999, "ab");
            fail("Expected VideoIdNotFoundException");
        } catch (VideoIdNotFoundException expected) {
            // expected
            System.out.println("=== VideoIdNotFoundException! ===");
        }
        checkVideoSame(201);
        checkVideoSame(202);
        checkVideoSame(203);

        // 全部删除重新计数
        // 先清掉 201 上的 "hello"
        network1.cleanSpamComments(201, "ell");
        network2.cleanSpamComments(201, "ell");
        // 现在 201 无评论

        network1.sendComment(20, 201, 11, "ab ab ab"); // cnt=3, 先插入
        network2.sendComment(20, 201, 11, "ab ab ab");
        network1.sendComment(30, 201, 22, "ab");        // cnt=1, 后插入
        network2.sendComment(30, 201, 22, "ab");
        network1.sendComment(20, 201, 33, "safe");
        network2.sendComment(20, 201, 33, "safe");

        assertArrayEquals(new int[] {2, 3}, network1.cleanSpamComments(201, "ab"));
        checkSideEffect(201);
        checkVideoSame(203);
        checkVideoSame(202);

        v201 = (Video) network1.getVideo(201);
        ids = v201.getCommentIds();
        contents = v201.getCommentContents();
        assertEquals(ids.length, contents.length);
        assertEquals(1, ids.length);
        assertEquals(33, ids[0]);
        assertEquals("safe", contents[0]);

        network2.cleanSpamComments(201, "ab"); // 同步对照组

        // 剩余评论不含 keyword
        assertArrayEquals(new int[] {0, 0}, network1.cleanSpamComments(201, "ab"));
        checkVideoSame(201);
        checkVideoSame(202);
        checkVideoSame(203);
        Video v201Phase6 = (Video) network1.getVideo(201); // 重新获取
        ids = v201Phase6.getCommentIds();
        contents = v201Phase6.getCommentContents();
        assertEquals(ids.length, contents.length);
        assertEquals(1, ids.length);
        assertEquals(33, ids[0]);
        assertEquals("safe", contents[0]);
    }

    private void genVideos(Network network) throws Exception {
        network.addUser(10, "uploader", 22);
        network.addUser(20, "viewer", 28);
        network.addUser(30, "follower", 35);
        network.addUser(40, "nobody", 21);
        network.uploadVideo(10, 201, "game");
        network.uploadVideo(10, 202, "food");
        network.watchVideo(20, 201);
        network.watchVideo(20, 202);
        network.followUser(30, 20);
        network.followUser(40, 10);
        network.followUser(40, 20);
        network.followUser(40, 30);
        network.uploadVideo(10, 203, "game");
        network.forwardVideo(20, 201, 30);
        network.forwardVideo(20, 202, 30);
        network.addUserCoins(20, 15);
        network.coinVideo(20, 201, 2);
        network.coinVideo(20, 202, 1);
        network.watchVideo(30, 201);
        network.addUserCoins(30, 50);
        network.likeVideo(30, 201);
        network.watchVideo(40, 201);
        network.watchVideo(40, 202);
        network.watchVideo(40, 203);
        network.addUserCoins(40, 50);
        network.coinVideo(40, 201, 2);
        network.coinVideo(40, 201, 1);
        network.coinVideo(40, 202, 2);
        network.coinVideo(40, 202, 1);
        network.coinVideo(40, 203, 2);
        network.coinVideo(40, 203, 1);
        network.likeVideo(40, 201);
        network.likeVideo(40, 202);
        network.likeVideo(40, 203);
    }

    private void checkSideEffect(int videoId) {
        VideoInterface v1 = network1.getVideo(videoId);
        VideoInterface v2 = network2.getVideo(videoId);
        assertEquals(v1.getId(), v2.getId());
        assertEquals(v1.getUploaderId(), v2.getUploaderId());
        assertEquals(v1.getType(), v2.getType());
        assertEquals(v1.getPlayCount(), v2.getPlayCount());
        assertEquals(v1.getCoins(), v2.getCoins());
        assertEquals(v1.getLikes(), v2.getLikes());
        assertEquals(v1.getForwardCount(), v2.getForwardCount());
        assertEquals(v1.getHeat(), v2.getHeat(), 0.0);
    }

    private void checkVideoSame(int videoId) {
        Video v1 = (Video) network1.getVideo(videoId);
        Video v2 = (Video) network2.getVideo(videoId);
        assertEquals(v1.getId(), v2.getId());
        assertEquals(v1.getUploaderId(), v2.getUploaderId());
        assertEquals(v1.getType(), v2.getType());
        assertEquals(v1.getPlayCount(), v2.getPlayCount());
        assertEquals(v1.getCoins(), v2.getCoins());
        assertEquals(v1.getLikes(), v2.getLikes());
        assertEquals(v1.getForwardCount(), v2.getForwardCount());
        assertEquals(v1.getHeat(), v2.getHeat(), 0.0);

        int[] ids1 = v1.getCommentIds();
        String[] strings1 = v1.getCommentContents();
        int[] ids2 = v2.getCommentIds();
        String[] strings2 = v2.getCommentContents();

        assertArrayEquals(ids1, ids2);         // 顺序+内容都验证
        assertArrayEquals(strings1, strings2); // 顺序+内容都验证
    }
}
