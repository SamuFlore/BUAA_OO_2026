import java.util.ArrayList;

public class Dispatcher {
    private Integer workerId;
    private ArrayList<Integer> storage;
    private static final int CAPACITY = 2;

    private Courier courier;

    public Dispatcher(Integer id) {
        this.workerId = id;
        this.storage = new ArrayList<>();
    }

    public Integer getWorkerId() {
        return workerId;
    }

    public void setCourier(Courier courier) {
        this.courier = courier;
    }

    public boolean receivePackage(int packageId) {
        if (storage.size() < CAPACITY) {
            storage.add(packageId);
            System.out.println("Dispatcher " + getWorkerId() +
                               " : receive package " + packageId);
            return true;
        } else {
            System.out.println("Dispatcher " + getWorkerId() +
                               " : no available storage space.");
            return false;
        }
    }

    public void arrangeSend(int packageId) {
        System.out.println("Dispatcher " + getWorkerId() +
                           " : arrange courier to send package " + packageId);
        courier.sendPackage(packageId);
    }

    public boolean takePackage(int packageId) {
        if (storage.contains(packageId)) {
            storage.remove(Integer.valueOf(packageId));
            System.out.println("Dispatcher " + getWorkerId() + " : take package " + packageId);
            return true;
        } else {
            System.out.println("Dispatcher " + getWorkerId() +
                               " : package " + packageId + " not found.");
            return false;
        }
    }

    public void showStorage() {
        // 打印仓库 可以用作调试
        System.out.println("STORAGE:");
        // 顶部边框
        System.out.print("┌───┬");

        for (int j = 0; j < CAPACITY; j++) {
            System.out.print("───────");
            if (j < CAPACITY - 1) {
                System.out.print("┬");
            }
        }
        System.out.println("┐");

        // 在每行开始添加仓库编号
        System.out.print("│S  │");
        for (int j = 0; j < CAPACITY; j++) {
            if (j < storage.size()) {
                System.out.printf(" %3d   ", storage.get(j));
            } else {
                System.out.print("       ");
            }
            if (j < CAPACITY - 1) {
                System.out.print("│");
            }
        }
        System.out.println("│");

        // 底部边框
        System.out.print("└───┴");
        for (int i = 0; i < CAPACITY; i++) {
            System.out.print("───────");
            if (i < CAPACITY - 1) {
                System.out.print("┴");
            }
        }
        System.out.println("┘");
    }
}