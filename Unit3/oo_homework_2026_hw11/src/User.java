import com.oocourse.spec3.main.UserInterface;
import com.oocourse.spec3.main.VideoInterface;

import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;

public class User implements UserInterface {
    private int id;
    private String name;
    private int age;
    private int coins;
    private HashMap<Integer, User> following;
    private HashMap<Integer, User> followers;
    private HashSet<Integer> medals;
    private HashMap<Integer, Integer> contributors; // K: userId, V: coins
    private LinkedList<Integer> receivedVideos;
    private HashSet<Integer> watchedVideos;
    private HashSet<Integer> likedVideos;
    private static final String[] types = new String[]{"tech", "music", "sport",
                                                       "game", "food", "travel", "comedy"}; // 类型枚举
    private int[] typeCounts;
    private int[] influence; // 各类视频热度缓存
    private ArrayList<Video> videos;

    public User(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.coins = 0;
        this.following = new HashMap<>();
        this.followers = new HashMap<>();
        this.medals = new HashSet<>();
        this.contributors = new HashMap<>();
        this.receivedVideos = new LinkedList<>();
        this.watchedVideos = new HashSet<>();
        this.likedVideos = new HashSet<>();
        this.typeCounts = new int[7];
        this.influence = new int[7];
        this.videos = new ArrayList<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    public void receiveVideo(Video video) {
        receivedVideos.addFirst(video.getId());
    }

    public void addFollower(User user) {
        followers.put(user.getId(), user);
    }

    public void removeFollower(User user) {
        followers.remove(user.getId());
    }

    public void follow(User user) {
        following.put(user.getId(), user);
    }

    public void unfollow(User user) {
        following.remove(user.getId());
    }

    public void watchVideo(int videoId) {
        watchedVideos.add(videoId);
        receivedVideos.removeIf(id -> id == videoId);
    }

    public void addCoins(int coins) {
        this.coins += coins;
    }

    public void addMedal(int uploaderId) {
        medals.add(uploaderId);
    }

    public void likeVideo(Video video) {
        likedVideos.add(video.getId());
        video.addLikes(1);
    }

    public void unlikeVideo(Video video) {
        likedVideos.remove(video.getId());
        video.addLikes(-1);
    }

    public Collection<User> getFollowing() {
        return following.values();
    }

    public Collection<User> getFollowers() {
        return followers.values();
    }

    public HashMap<Integer, Integer>  getContributors() {
        return contributors;
    }

    public boolean emptyContributor() {
        return contributors.isEmpty();
    }

    public void addContribution(int contributorId, int amount) {
        Integer contribution = contributors.get(contributorId);
        if (contribution == null) {
            contributors.put(contributorId, amount);
        }
        else {
            contributors.put(contributorId, contribution + amount);
        }
    }

    @Override
    public boolean isFollowing(UserInterface user) {
        return following.containsKey(user.getId());
    }

    @Override
    public boolean containsFollower(UserInterface user) {
        return followers.containsKey(user.getId());
    }

    @Override
    public boolean hasReceivedVideo(VideoInterface video) {
        return receivedVideos.contains(video.getId());
    }

    @Override
    public double[] queryAgeRatio() {
        double[] ratio = new double[4];
        if (followers.size() == 0) {
            ratio[0] = 0.0;
            ratio[1] = 0.0;
            ratio[2] = 0.0;
            ratio[3] = 0.0;
            return ratio;
        }
        int seg1Num = 0;
        int seg2Num = 0;
        int seg3Num = 0;
        int seg4Num = 0;
        for (User u : followers.values()) {
            if (u.getAge() <= 16) { seg1Num++; }
            if (u.getAge() >= 17 && u.getAge() <= 30) { seg2Num++; }
            if (u.getAge() >= 31 && u.getAge() <= 45) { seg3Num++; }
            if (u.getAge() >= 46)  { seg4Num++; }
        }
        ratio[0] = (double) seg1Num / followers.size();
        ratio[1] = (double) seg2Num / followers.size();
        ratio[2] = (double) seg3Num / followers.size();
        ratio[3] = (double) seg4Num / followers.size();
        return ratio;
    }

    @Override
    public boolean hasWatchedVideo(VideoInterface video) {
        return watchedVideos.contains(video.getId());
    }

    public boolean noWatchedVideo() {
        return watchedVideos.isEmpty();
    }

    @Override
    public boolean hasLikedVideo(VideoInterface video) {
        return likedVideos.contains(video.getId());
    }

    @Override
    public int getCoins() { return coins; }

    @Override
    public boolean hasMedal(int uploaderId) {
        return medals.contains(uploaderId);
    }

    @Override
    public List<Integer> queryReceivedUnwatchedVideos() {
        List<Integer> res = new ArrayList<>();
        int cnt = Math.min(receivedVideos.size(), 5);
        for (int i = 0; i < cnt; i++) {
            res.add(receivedVideos.get(i));
        }
        return res;
    }

    private static int typeIndex(String type) {
        switch (type) {
            case "tech": return 0;
            case "music": return 1;
            case "sport": return 2;
            case "game": return 3;
            case "food": return 4;
            case "travel": return 5;
            case "comedy": return 6;
            default: return -1;
        }
    }

    public void addTypeCnt(String type) {
        int index =  typeIndex(type);
        typeCounts[index]++;
    }

    public void addVideo(Video video) {
        videos.add(video);
    }

    @Override
    public int getInterest(String type, int totalVideos) {
        int index = typeIndex(type);
        return typeCounts[index] * (totalVideos - watchedVideos.size() + 1);
    }

    public void addInfluence(String type, int value) {
        // watch: +2
        // like: +3
        // unlike: -3
        // coin: +(amount * 5)
        // forward: +4
        int index = typeIndex(type);
        influence[index] +=  value;
    }

    @Override
    public int getInfluence(String type) {
        int index = typeIndex(type);
        return influence[index];
    }

    @Override
    public List<Integer> getProfile(int totalVideos) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            res.add(getInterest(types[i], totalVideos));
        }
        return res;
    }

    @Override
    public long computeUpScore(UserInterface up, int totalVideos) {
        long res = 0;
        for (int i = 0; i < 7; i++) {
            res += (long) getInterest(types[i], totalVideos) * up.getInfluence(types[i]);
        }
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        boolean flag = false;
        if (obj != null && obj instanceof UserInterface) {
            flag = ((UserInterface) obj).getId() == this.id;
        }
        else if (obj == null || !(obj instanceof UserInterface)) {
            flag = false;
        }
        return flag;
    }

}