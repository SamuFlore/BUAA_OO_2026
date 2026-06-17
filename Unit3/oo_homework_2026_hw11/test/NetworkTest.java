import com.oocourse.spec3.exceptions.ColdStartUserException;
import com.oocourse.spec3.exceptions.InvalidRankException;
import com.oocourse.spec3.exceptions.NoVideoUploadedException;
import com.oocourse.spec3.exceptions.UserIdNotFoundException;
import com.oocourse.spec3.main.UserInterface;
import com.oocourse.spec3.main.VideoInterface;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class NetworkTest {
    Network network1;
    Network network2;

    private void genVideos(Network network) throws Exception {
        // User: 10, 20, 30, 40
        // Video: 201, 202, 203
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
    }

    // =============== recommend_Nth_up =====================
    @Test
    public void testRecommendNthUp() throws Exception {
        network1 = new Network();
        network2 = new Network();
        genVideos(network1);
        genVideos(network2);
        // ===== Exception: UserIdNotFoundException =====
        try {
            network1.recommendNthUp(999, 1);
            fail("Expected UserIdNotFoundException");
        } catch (UserIdNotFoundException e) { /* expected */ }
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        // ===== Exception: InvalidRankException (rank=0, rank=-1) =====
        try {
            network1.recommendNthUp(10, 0);
            fail("Expected InvalidRankException for rank=0");
        } catch (InvalidRankException e) { /* expected */ }
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        try {
            network1.recommendNthUp(10, -1);
            fail("Expected InvalidRankException for rank=-1");
        } catch (InvalidRankException e) { /* expected */ }
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        // edge: userId not exist AND rank <= 0 → UserIdNotFoundException takes priority
        try {
            network1.recommendNthUp(999, -1);
            fail("Expected UserIdNotFoundException (not InvalidRankException)");
        } catch (UserIdNotFoundException e) { /* expected */ }
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        // ===== Exception: NoVideoUploadedException =====
        Network tmp1 = new Network();
        Network tmp2 = new Network();
        tmp1.addUser(10, "uploader", 22);
        tmp2.addUser(10, "uploader", 22);
        try {
            tmp1.recommendNthUp(10, 1);
            fail("Expected NoVideoUploadedException");
        } catch (NoVideoUploadedException e) { /* expected */ }
        checkUserSameTmp(tmp1, tmp2, 10);

        // ===== Exception: ColdStartUserException =====
        // user 40 follows 10, 20, 30 → 0 candidates → ColdStart
        try {
            network1.recommendNthUp(40, 1);
            fail("Expected ColdStartUserException for user 40 (no candidates)");
        } catch (ColdStartUserException e) { /* expected */ }
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        // user 30 follows 20 → 2 candidates (10, 40), rank=3 → ColdStart
        try {
            network1.recommendNthUp(30, 3);
            fail("Expected ColdStartUserException for user 30 rank 3");
        } catch (ColdStartUserException e) { /* expected */ }
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        // ===== Correctness: User 20 (follows nobody, candidates: 10, 30, 40) =====
        assertEquals(10, network1.recommendNthUp(20, 1));
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        assertEquals(30, network1.recommendNthUp(20, 2));
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        assertEquals(40, network1.recommendNthUp(20, 3));
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        // ===== Correctness: User 10 (follows nobody, candidates: 20, 30, 40) =====
        assertEquals(20, network1.recommendNthUp(10, 1));
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        assertEquals(30, network1.recommendNthUp(10, 2));
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        assertEquals(40, network1.recommendNthUp(10, 3));
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        // ===== Correctness: User 30 (follows 20, candidates: 10, 40) =====
        assertEquals(10, network1.recommendNthUp(30, 1));
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        assertEquals(40, network1.recommendNthUp(30, 2));
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});

        // ===== JML ensures verification =====
        verifyRecommendResult(20, 1, 10);
        verifyRecommendResult(20, 2, 30);
        verifyRecommendResult(20, 3, 40);
        verifyRecommendResult(10, 1, 20);
        verifyRecommendResult(10, 2, 30);
        verifyRecommendResult(10, 3, 40);
        verifyRecommendResult(30, 1, 10);
        verifyRecommendResult(30, 2, 40);

        // ===== Idempotent =====
        int first  = network1.recommendNthUp(20, 2);
        int second = network1.recommendNthUp(20, 2);
        int third  = network1.recommendNthUp(20, 2);
        assertEquals(first, second);
        assertEquals(second, third);
        checkAllSame(new int[]{10, 20, 30, 40}, new int[]{201, 202, 203});
    }

    // ===== Helper =====

    private void checkAllSame(int[] userIds, int[] videoIds) {
        for (int uid : userIds) {
            checkUserSame(uid);
        }
        for (int vid : videoIds) {
            checkVideoSame(vid);
        }
    }

    private void checkUserSame(int userId) {
        UserInterface u1 = network1.getUser(userId);
        UserInterface u2 = network2.getUser(userId);

        // Basic
        assertEquals("id", u1.getId(), u2.getId());
        assertEquals("name", u1.getName(), u2.getName());
        assertEquals("age", u1.getAge(), u2.getAge());
        assertEquals("coins", u1.getCoins(), u2.getCoins());

        // Following / followers for all other users in the network
        UserInterface[] allUsers1 = network1.getUsers();
        UserInterface[] allUsers2 = network2.getUsers();
        assertEquals("user count", allUsers1.length, allUsers2.length);

        HashMap<Integer, UserInterface> map2 = new HashMap<>();
        for (UserInterface u : allUsers2) {
            map2.put(u.getId(), u);
        }

        for (UserInterface other1 : allUsers1) {
            int oid = other1.getId();
            if (oid == userId) {
                continue;
            }
            UserInterface other2 = map2.get(oid);
            assertNotNull("user " + oid + " missing in network2", other2);
            assertEquals("isFollowing(" + oid + ") for user " + userId,
                    u1.isFollowing(other1), u2.isFollowing(other2));
            assertEquals("containsFollower(" + oid + ") for user " + userId,
                    u1.containsFollower(other1), u2.containsFollower(other2));
        }

        // Medal for all uploaders
        for (UserInterface other1 : allUsers1) {
            int oid = other1.getId();
            assertEquals("hasMedal(" + oid + ") for user " + userId,
                    u1.hasMedal(oid), u2.hasMedal(oid));
        }

        // Videos: watched, liked, received
        for (int vid : new int[]{201, 202, 203}) {
            VideoInterface v1 = network1.getVideo(vid);
            VideoInterface v2 = network2.getVideo(vid);
            assertNotNull("video " + vid + " missing in network1", v1);
            assertNotNull("video " + vid + " missing in network2", v2);
            assertEquals("hasWatchedVideo(" + vid + ") for user " + userId,
                    u1.hasWatchedVideo(v1), u2.hasWatchedVideo(v2));
            assertEquals("hasLikedVideo(" + vid + ") for user " + userId,
                    u1.hasLikedVideo(v1), u2.hasLikedVideo(v2));
            assertEquals("hasReceivedVideo(" + vid + ") for user " + userId,
                    u1.hasReceivedVideo(v1), u2.hasReceivedVideo(v2));
        }

        // Interest and influence for all 7 types
        String[] types = new String[]{"tech", "music", "sport", "game",
                                      "food", "travel", "comedy"};
        for (String type : types) {
            assertEquals("getInterest(" + type + ") for user " + userId,
                    u1.getInterest(type, 3), u2.getInterest(type, 3));
            assertEquals("getInfluence(" + type + ") for user " + userId,
                    u1.getInfluence(type), u2.getInfluence(type));
        }

        // queryAgeRatio
        double[] ratio1 = u1.queryAgeRatio();
        double[] ratio2 = u2.queryAgeRatio();
        assertEquals("queryAgeRatio length for user " + userId,
                ratio1.length, ratio2.length);
        for (int i = 0; i < ratio1.length; i++) {
            assertEquals("queryAgeRatio[" + i + "] for user " + userId,
                    ratio1[i], ratio2[i], 0.0);
        }

        // queryReceivedUnwatchedVideos
        List<Integer> rv1 = u1.queryReceivedUnwatchedVideos();
        List<Integer> rv2 = u2.queryReceivedUnwatchedVideos();
        assertEquals("queryReceivedUnwatchedVideos size for user " + userId,
                rv1.size(), rv2.size());
        for (int i = 0; i < rv1.size(); i++) {
            assertEquals("queryReceivedUnwatchedVideos[" + i + "] for user " + userId,
                    rv1.get(i), rv2.get(i));
        }

        // getProfile
        List<Integer> p1 = u1.getProfile(3);
        List<Integer> p2 = u2.getProfile(3);
        assertEquals("getProfile size for user " + userId, p1.size(), p2.size());
        for (int i = 0; i < p1.size(); i++) {
            assertEquals("getProfile[" + i + "] for user " + userId,
                    p1.get(i), p2.get(i));
        }
    }

    private void checkUserSameTmp(Network net1, Network net2, int userId) {
        UserInterface u1 = net1.getUser(userId);
        UserInterface u2 = net2.getUser(userId);
        assertEquals(u1.getId(), u2.getId());
        assertEquals(u1.getName(), u2.getName());
        assertEquals(u1.getAge(), u2.getAge());
        assertEquals(u1.getCoins(), u2.getCoins());
    }

    private void verifyRecommendResult(int userId, int rank, int result) {
        assertTrue(network1.containsUser(result));
        assertNotEquals(userId, result);
        UserInterface user = network1.getUser(userId);
        assertFalse(user.isFollowing(network1.getUser(result)));

        UserInterface[] allUsers = network1.getUsers();
        long resultScore = user.computeUpScore(network1.getUser(result), 3);
        int ahead = 0;
        for (UserInterface u : allUsers) {
            if (u.getId() != userId && !user.isFollowing(u)) {
                long score = user.computeUpScore(u, 3);
                if (score > resultScore ||
                        (score == resultScore && u.getId() < result)) {
                    ahead++;
                }
            }
        }
        assertEquals(rank - 1, ahead);
    }
}
