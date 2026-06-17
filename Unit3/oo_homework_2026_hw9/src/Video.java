import com.oocourse.spec1.main.VideoInterface;

public class Video implements VideoInterface {
    private int id;
    private int uploaderId;

    public Video(int id, int uploaderId) {
        this.id = id;
        this.uploaderId = uploaderId;
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