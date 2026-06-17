import com.oocourse.spec1.exceptions.EqualUserIdException;
import com.oocourse.spec1.exceptions.InvalidAgeException;
import com.oocourse.spec1.exceptions.UserIdNotFoundException;
import com.oocourse.spec1.exceptions.EqualVideoIdException;
import com.oocourse.spec1.exceptions.SelfSubscriptionException;
import com.oocourse.spec1.exceptions.DuplicateSubscriptionException;
import com.oocourse.spec1.exceptions.FollowLinkNotFoundException;
import com.oocourse.spec1.exceptions.VideoIdNotFoundException;
import com.oocourse.spec1.exceptions.UncessException;
import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.UserInterface;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

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
    public void uploadVideo(int uploaderId, int videoId)
            throws UserIdNotFoundException, EqualVideoIdException {
        if (!containsUser(uploaderId)) {
            throw new UserIdNotFoundException(uploaderId);
        }
        if (containsUser(uploaderId) && containsVideo(videoId)) {
            throw new EqualVideoIdException(videoId);
        }
        Video video = new Video(videoId, uploaderId);
        videos.put(videoId, video);
        User up = users.get(uploaderId);
        for (User u : up.getFollowers()) {
            u.receiveVideo(video);
        }
        System.out.println("upload_video succeeded");
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

    public UserInterface[] getUsers() {
        return users.values().toArray(new UserInterface[0]);
    }

}