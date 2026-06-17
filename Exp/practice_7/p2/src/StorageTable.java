import java.util.ArrayList;

public class StorageTable {
    private final ArrayList<ArrayList<Integer>> storageTable;
    // 假设有2间仓库，每个仓库的最大容量为2
    private static final int CAPACITY = 2;
    private static final int STORAGE_CNT = 2;

    public StorageTable() {
        this.storageTable = new ArrayList<>();
        for (int i = 0; i < STORAGE_CNT; i++) {
            storageTable.add(new ArrayList<>());
        }
    }

    public int findSpareStorage() {
        for (int i = 0; i < storageTable.size(); i++) {
            if (storageTable.get(i).size() < CAPACITY) {
                return i;
            }
        }
        return -1;
    }

    public void addPackage(int storageId, int packageId) {
        storageTable.get(storageId).add(packageId);
    }

    public boolean hasPackage(int packageId) {
        for (ArrayList<Integer> storage : storageTable) {
            for (Integer integer : storage) {
                if (integer == packageId) {
                    return true;
                }
            }
        }
        return false;
    }

    public int takePackage(int packageId) {
        for (int i = 0; i < storageTable.size(); i++) {
            ArrayList<Integer> storage = storageTable.get(i);
            for (int j = 0; j < storage.size(); j++) {
                if (storage.get(j) == packageId) {
                    storage.remove(j);
                    return i + 1;
                }
            }
        }
        // 出错
        return -1;
    }

    public void show() {
        // 打印仓库 可以用作调试
        System.out.println("STORAGE TABLE:");

        // 顶部边框
        System.out.print("┌───┬");

        for (int j = 0; j < CAPACITY; j++) {
            System.out.print("───────");
            if (j < CAPACITY - 1)
                System.out.print("┬");
        }
        System.out.println("┐");

        // 仓库内容
        for (int i = 0; i < storageTable.size(); i++) {
            ArrayList<Integer> storage = storageTable.get(i);

            // 在每行开始添加仓库编号
            System.out.printf("│S%d │", i + 1);
            for (int j = 0; j < CAPACITY; j++) {
                if (j < storage.size()) {
                    System.out.printf(" %3d   ", storage.get(j));
                } else {
                    System.out.print("       ");
                }
                if (j < CAPACITY - 1)
                    System.out.print("│");
            }
            System.out.println("│");

            // 仓库之间的分隔线
            if (i < storageTable.size() - 1) {
                System.out.print("├───┼");
                for (int k = 0; k < CAPACITY; k++) {
                    System.out.print("───────");
                    if (k < CAPACITY - 1)
                        System.out.print("┼");
                }
                System.out.println("┤");
            }
        }

        // 底部边框
        System.out.print("└───┴");
        for (int i = 0; i < CAPACITY; i++) {
            System.out.print("───────");
            if (i < CAPACITY - 1)
                System.out.print("┴");
        }
        System.out.println("┘");
    }

}
