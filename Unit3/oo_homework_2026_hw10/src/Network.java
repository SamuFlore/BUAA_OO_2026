import com.oocourse.spec2.exceptions.DuplicateMedalException;
import com.oocourse.spec2.exceptions.DuplicateSubscriptionException;
import com.oocourse.spec2.exceptions.EqualCommentIdException;
import com.oocourse.spec2.exceptions.EqualUserIdException;
import com.oocourse.spec2.exceptions.EqualVideoIdException;
import com.oocourse.spec2.exceptions.FollowLinkNotFoundException;
import com.oocourse.spec2.exceptions.InsufficientCoinsException;
import com.oocourse.spec2.exceptions.InvalidAgeException;
import com.oocourse.spec2.exceptions.InvalidCoinsException;
import com.oocourse.spec2.exceptions.InvalidCommentException;
import com.oocourse.spec2.exceptions.InvalidTypeException;
import com.oocourse.spec2.exceptions.NoContributorsException;
import com.oocourse.spec2.exceptions.SelfSubscriptionException;
import com.oocourse.spec2.exceptions.UncessException;
import com.oocourse.spec2.exceptions.UserIdNotFoundException;
import com.oocourse.spec2.exceptions.VideoIdNotFoundException;
import com.oocourse.spec2.exceptions.VideoUnwatchedException;

import com.oocourse.spec2.main.NetworkInterface;
import com.oocourse.spec2.main.UserInterface;
import com.oocourse.spec2.main.VideoInterface;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Arrays;

public class Network implements NetworkInterface {
    private HashMap<Integer, User> users;
    private HashMap<Integer, Video> videos;

    private int mutualFollowingCnt; // 互关总数

    public Network() {
        users = new HashMap<>();
        videos = new HashMap<>();
        mutualFollowingCnt = 0;
    }

    @Override
    public boolean containsUser(int id) {
        return users.containsKey(id);
    }

    @Override
    public User getUser(int id) {
        return users.get(id);
    }

    @Override
    public boolean containsVideo(int id) {
        return videos.containsKey(id);
    }

    @Override
    public Video getVideo(int id) {
        return videos.get(id);
    }

    @Override
    public void addUser(int id, String name, int age)
            throws EqualUserIdException, InvalidAgeException {
        if (containsUser(id)) {
            throw new EqualUserIdException(id);
        }
        if (!containsUser(id) && (age < 0 || age > 110)) {
            throw new InvalidAgeException(age);
        }
        User user = new User(id, name, age);
        users.put(id, user);
        System.out.println("add_user succeeded");
    }

    @Override
    public void uploadVideo(int uploaderId, int videoId, String type)
            throws UserIdNotFoundException, EqualVideoIdException, InvalidTypeException {
        if (!containsUser(uploaderId)) {
            throw new UserIdNotFoundException(uploaderId);
        }
        if (containsUser(uploaderId) && containsVideo(videoId)) {
            throw new EqualVideoIdException(videoId);
        }
        if (containsUser(uploaderId) && !containsVideo(videoId) && !isValidType(type)) {
            throw new InvalidTypeException(type);
        }
        Video video = new Video(videoId, uploaderId, type);
        videos.put(videoId, video);
        User up = users.get(uploaderId);
        for (User u : up.getFollowers()) {
            u.receiveVideo(video);
        }
        System.out.println("upload_video succeeded");
    }

    @Override
    public boolean isValidType(String type) {
        return (type.equals("tech") || type.equals("music") || type.equals("sport") ||
                type.equals("game") || type.equals("food") || type.equals("travel") ||
                type.equals("comedy"));
    }

    @Override
    public void followUser(int id1, int id2)
            throws UserIdNotFoundException,
                    SelfSubscriptionException,
                    DuplicateSubscriptionException {
        if (!containsUser(id1)) {
            throw new UserIdNotFoundException(id1);
        }
        if (containsUser(id1) && !containsUser(id2)) {
            throw new UserIdNotFoundException(id2);
        }
        if (containsUser(id1) && containsUser(id2) && id1 == id2) {
            throw new SelfSubscriptionException(id1);
        }
        if (containsUser(id1) && containsUser(id2) &&
                id1 != id2 && getUser(id1).isFollowing(getUser(id2))) {
            throw new DuplicateSubscriptionException(id1, id2);
        }
        getUser(id2).addFollower(getUser(id1));
        getUser(id1).follow(getUser(id2));

        if (getUser(id2).isFollowing(getUser(id1))) { // 确认互关
            mutualFollowingCnt++;
        }
        System.out.println("follow_user succeeded");
    }

    @Override
    public void unfollowUser(int id1, int id2)
            throws UserIdNotFoundException, FollowLinkNotFoundException {
        if (!containsUser(id1)) {
            throw new UserIdNotFoundException(id1);
        }
        if (containsUser(id1) && !containsUser(id2)) {
            throw new UserIdNotFoundException(id2);
        }
        if (containsUser(id1) && containsUser(id2) && !getUser(id1).isFollowing(getUser(id2))) {
            throw new FollowLinkNotFoundException(id1, id2);
        }
        if (getUser(id2).isFollowing(getUser(id1))) { // 取消互关
            mutualFollowingCnt--;
        }
        getUser(id1).unfollow(getUser(id2));
        getUser(id2).removeFollower(getUser(id1));
        System.out.println("unfollow_user succeeded");
    }

    @Override
    public void watchVideo(int userId, int videoId)
            throws UserIdNotFoundException, VideoIdNotFoundException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (containsUser(userId) && !containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        getUser(userId).watchVideo(videoId);
        videos.get(videoId).addPlayCount(1);
        System.out.println("watch_video succeeded");
    }

    @Override
    public List<Integer> queryReceivedUnwatchedVideos(int userId)
            throws UserIdNotFoundException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        List<Integer> res = getUser(userId).queryReceivedUnwatchedVideos();
        return res;
    }

    @Override
    public double[] queryUpFollowersAgeRatio(int upId)
            throws UserIdNotFoundException {
        if (!containsUser(upId)) {
            throw new UserIdNotFoundException(upId);
        }
        double[] res = getUser(upId).queryAgeRatio();
        return res;
    }

    @Override
    public int queryMutualFollowingSum() {
        return mutualFollowingCnt;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
            throws UserIdNotFoundException, UncessException {
        if (!containsUser(id1)) {
            throw new UserIdNotFoundException(id1);
        }
        if (containsUser(id1) && !containsUser(id2)) {
            throw new UserIdNotFoundException(id2);
        }
        if (id1 == id2) {
            return 0;
        }
        
        Queue<User> queue = new LinkedList<>();
        HashMap<Integer, Integer> visited = new HashMap<>();
        queue.add(getUser(id1));
        visited.put(id1, 0);

        while (!queue.isEmpty()) {
            User cur = queue.poll();
            int nextSteps = visited.get(cur.getId()) + 1;
            for (User next : cur.getFollowing()) {
                int nextId = next.getId();
                if (nextId == id2) {
                    return nextSteps;
                }
                if (!visited.containsKey(nextId)) {
                    visited.put(nextId, nextSteps);
                    queue.add(next);
                }
            }
        }
        throw new UncessException(id1, id2);
    }

    @Override
    public void addUserCoins(int userId, int coins) throws UserIdNotFoundException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        getUser(userId).addCoins(coins);
        System.out.println("add_user_coins succeeded");
    }

    @Override
    public void likeVideo(int userId, int videoId)
            throws UserIdNotFoundException, VideoIdNotFoundException,
                   VideoUnwatchedException, EqualUserIdException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        if (containsVideo(videoId) && containsUser(userId) &&
            userId == getVideo(videoId).getUploaderId()) {
            throw new EqualUserIdException(userId);
        }
        if (!getUser(userId).hasWatchedVideo(getVideo(videoId))) {
            throw new VideoUnwatchedException(userId, videoId);
        }
        User user = getUser(userId);
        Video video = getVideo(videoId);
        if (user.hasLikedVideo(video)) {
            user.unlikeVideo(video);
            System.out.println("unlike_video succeeded");
        }
        else {
            user.likeVideo(video);
            System.out.println("like_video succeeded");
        }
    }

    @Override
    public void coinVideo(int userId, int videoId, int amount)
            throws UserIdNotFoundException, VideoIdNotFoundException, InsufficientCoinsException,
                   VideoUnwatchedException, InvalidCoinsException, EqualUserIdException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        if (userId == getVideo(videoId).getUploaderId()) {
            throw new EqualUserIdException(userId);
        }
        if (!getUser(userId).hasWatchedVideo(getVideo(videoId))) {
            throw new VideoUnwatchedException(userId, videoId);
        }
        if (amount != 1 && amount != 2) {
            throw new InvalidCoinsException(amount);
        }
        if (getUser(userId).getCoins() < amount) {
            throw new InsufficientCoinsException(userId);
        }
        Video video = getVideo(videoId);
        User user = getUser(userId);
        User up = getUser(video.getUploaderId());
        video.addCoins(amount);
        user.addCoins(-amount);
        up.addCoins(amount);
        up.addContribution(userId, amount);
        System.out.println("coin_video succeeded");
    }

    @Override
    public int queryBestContributor(int id)
            throws UserIdNotFoundException, NoContributorsException {
        if (!containsUser(id)) {
            throw new UserIdNotFoundException(id);
        }
        if (getUser(id).emptyContributor()) {
            throw new NoContributorsException(id);
        }

        int maxAmount = -1;
        int minId = Integer.MAX_VALUE;
        HashMap<Integer, Integer> contributors = getUser(id).getContributors();
        for (HashMap.Entry<Integer, Integer> entry : contributors.entrySet()) {
            int userId = entry.getKey();
            int contribution = entry.getValue();
            if (contribution > maxAmount) {
                maxAmount = contribution;
                minId = userId;
            }
            else if (contribution == maxAmount) {
                minId = Math.min(userId, minId);
            }
        }
        return minId;
    }

    @Override
    public void forwardVideo(int userId, int videoId, int followerId)
            throws UserIdNotFoundException, VideoIdNotFoundException,
                   FollowLinkNotFoundException, VideoUnwatchedException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsUser(followerId)) {
            throw new UserIdNotFoundException(followerId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        if (!getUser(userId).hasWatchedVideo(getVideo(videoId))) {
            throw new VideoUnwatchedException(userId, videoId);
        }
        if (!getUser(userId).containsFollower(getUser(followerId))) {
            throw new FollowLinkNotFoundException(userId, followerId);
        }
        User follower = getUser(followerId);
        Video video = getVideo(videoId);
        follower.receiveVideo(video);
        video.addForwardCount(1);
        System.out.println("forward_video succeeded");
    }

    @Override
    public void sendComment(int userId, int videoId, int commentId, String comment)
            throws UserIdNotFoundException, VideoIdNotFoundException,
                   EqualCommentIdException, InvalidCommentException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        if (getVideo(videoId).containsComment(commentId)) {
            throw new EqualCommentIdException(commentId);
        }
        if (comment == null || comment.equals("")) {
            throw new InvalidCommentException();
        }
        Video video =  getVideo(videoId);
        video.addComment(commentId, comment);
        System.out.println("send_comment succeeded");
    }

    @Override
    public int[] cleanSpamComments(int videoId, String keyword) throws VideoIdNotFoundException {
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        Video video = getVideo(videoId);
        int removed = 0;
        int maxCount = 0;
        LinkedHashMap<Integer, String> comments = video.getComments();
        Iterator<HashMap.Entry<Integer, String>> iter = comments.entrySet().iterator();
        while (iter.hasNext()) {
            HashMap.Entry<Integer, String> entry = iter.next();
            String comment = entry.getValue();
            if (comment.contains(keyword)) {
                removed++;
                int cnt = calOccurrence(comment, keyword);
                if (cnt > maxCount) { maxCount = cnt; }
                iter.remove();
            }
        }
        int[] res = new int[2];
        res[0] = removed;
        res[1] = (res[0] == 0) ? 0 : maxCount;
        return res;
    }

    private int calOccurrence(String comment, String keyword) {
        int len =  comment.length() - keyword.length();
        int cnt = 0;
        for (int i = 0; i <= len; i++) {
            if (comment.startsWith(keyword, i)) {
                cnt++;
            }
        }
        return cnt;
    }

    @Override
    public VideoInterface queryMostPopularVideo(String type) throws InvalidTypeException {
        if (!isValidType(type)) {
            throw new InvalidTypeException(type);
        }
        double maxHeat = -1.0;
        int minId = Integer.MAX_VALUE;
        for (HashMap.Entry<Integer, Video> entry : videos.entrySet()) {
            int videoId = entry.getKey();
            Video video = entry.getValue();
            if (!video.getType().equals(type)) { continue; }
            if (video.getHeat() > maxHeat) {
                maxHeat = video.getHeat();
                minId = videoId;
            }
            else if (video.getHeat() == maxHeat) {
                minId = Math.min(minId, videoId);
            }
        }
        if (minId == Integer.MAX_VALUE) { return null; }
        return getVideo(minId);
    }

    @Override
    public void purchaseMedal(int userId, int videoId, int amount) throws
            UserIdNotFoundException, VideoIdNotFoundException, EqualUserIdException,
            InsufficientCoinsException, DuplicateMedalException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        if (userId == getVideo(videoId).getUploaderId()) {
            throw new EqualUserIdException(userId);
        }
        if (getUser(userId).getCoins() < amount) {
            throw new InsufficientCoinsException(userId);
        }
        if (getUser(userId).hasMedal(getVideo(videoId).getUploaderId())) {
            throw new DuplicateMedalException(userId, getVideo(videoId).getUploaderId());
        }
        User user = getUser(userId);
        User up = getUser(getVideo(videoId).getUploaderId());
        user.addCoins(-amount);
        user.addMedal(up.getId());
        up.addCoins(amount);
        System.out.println("purchase_medal succeeded");
    }

    @Override
    public int queryLongestDecSeq() { // DAG
        if (users.size() == 0) {
            return 0;
        }
        User[] sorted = users.values().toArray(new User[0]);
        Arrays.sort(sorted, (a, b) -> Integer.compare(a.getAge(), b.getAge()));

        HashMap<Integer, Integer> dp = new HashMap<>();
        int maxLen = 0;
        for (User u : sorted) {
            int best = 1;
            for (User v : u.getFollowing()) {
                if (u.getAge() > v.getAge()) {
                    best = Math.max(best, dp.get(v.getId()) + 1);
                }
            }
            dp.put(u.getId(), best);
            maxLen = Math.max(maxLen, best);
        }
        return maxLen;
    }

    public UserInterface[] getUsers() {
        return users.values().toArray(new UserInterface[0]);
    }

}