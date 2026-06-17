import com.oocourse.spec3.main.VideoInterface;

import java.util.LinkedHashMap;

public class Video implements VideoInterface {
    private int id;
    private int uploaderId;
    private String type;
    private int playCount;
    private int likes;
    private int forwardCount;
    private int coins;
    private LinkedHashMap<Integer, String> comments; // K: commentId, V: commentContent

    public Video(int id, int uploaderId, String type) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.type = type;
        this.playCount = 0;
        this.likes = 0;
        this.forwardCount = 0;
        this.coins = 0;
        this.comments = new LinkedHashMap<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getUploaderId() {
        return uploaderId;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getPlayCount() {
        return playCount;
    }

    public void addPlayCount(int playCount) {
        this.playCount += playCount;
    }

    public void addLikes(int likes) {
        this.likes += likes;
    }

    public void addCoins(int coins) {
        this.coins += coins;
    }

    public void addForwardCount(int forwardCount) {
        this.forwardCount += forwardCount;
    }

    public void addComment(int commentId, String comment) {
        this.comments.put(commentId, comment);
    }

    public LinkedHashMap<Integer, String> getComments() {
        return comments;
    }

    public int[] getCommentIds() {
        int[] ids = new int[comments.size()];
        int i = 0;
        for (int id : comments.keySet()) {
            ids[i++] = id;
        }
        return ids;
    }

    public String[] getCommentContents() {
        return comments.values().toArray(new String[0]);
    }

    @Override
    public int getLikes() {
        return likes;
    }

    @Override
    public int getForwardCount() {
        return forwardCount;
    }

    @Override
    public int getCoins() {
        return coins;
    }

    @Override
    public int getHeat() {
        return (playCount * 2 + likes * 3 + forwardCount * 4 + coins * 5);
    }

    @Override
    public boolean containsComment(int id) {
        return comments.containsKey(id);
    }

    @Override
    public boolean equals(Object obj) {
        boolean flag = false;
        if (obj != null && obj instanceof VideoInterface) {
            flag = (((VideoInterface) obj).getId() == id);
        }
        else if (obj == null || !(obj instanceof VideoInterface)) {
            flag = false;
        }
        return flag;
    }
}