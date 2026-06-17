import java.util.ArrayList;

public class Dispatcher extends Worker {
    private StorageTable storageTable;
    private CourierTable courierTable;
    private ArrayList<Integer> sendList; // 当前还未送的快递列表
    private ArrayList<Integer> receiveList; // 当前还装进仓库的快递列表

    public Dispatcher(Integer id) {
        super(id);
        this.sendList = new ArrayList<>();
        this.receiveList = new ArrayList<>();
        this.storageTable = new StorageTable();
    }

    public void setCourier(CourierTable courierTable) {
        this.courierTable = courierTable;
    }

    public boolean receivePackage(int packageId) {
        int storageId = storageTable.findSpareStorage();
        if (storageId == -1) {
            receiveList.add(packageId);
            System.out.println("Dispatcher " + getWorkerId() +
                               " : add package " + packageId + " to receive list.");
            return false;
        } else {
            storageTable.addPackage(storageId, packageId);
            System.out.println("Dispatcher " + getWorkerId() +
                               " : receive package " + packageId + " in storage " + (storageId + 1));
            return true;
        }
    }

    public void arrangeSend(int packageId) {
        Courier courier = courierTable.findSpareCourier();
        if (courier != null) {
            System.out.println("Dispatcher " + getWorkerId() +
                               " : arrange courier " + courier.getWorkerId() +
                               " to send package " + packageId);
            courier.sendPackage(packageId);
        } else {
            sendList.add(packageId);
            System.out.println("Dispatcher " + getWorkerId() +
                               " : add package " + packageId + " to send list.");
        }
    }

    public boolean takePackage(int packageId) {
        if (storageTable.hasPackage(packageId)) {
            int storageId = storageTable.takePackage(packageId);
            System.out.println("Dispatcher " + getWorkerId() +
                               " : take package " + packageId + " from storage " + storageId);
            if (!receiveList.isEmpty()) {
                int id = receiveList.get(0);
                receiveList.remove(0);
                receivePackage(id);
            }
            return true;
        } else {
            System.out.println("Dispatcher " + getWorkerId() +
                               " : package " + packageId + " not found.");
            return false;
        }
    }

    public void courierComeBack(int courierId) {
        boolean b = courierTable.comeBack(courierId);
        // 若快递员确认返回且有快递未送，则立刻再次启程
        if (b && !sendList.isEmpty()) {
            int packageId = sendList.get(0);
            sendList.remove(0);
            arrangeSend(packageId);
        }
    }

    public void showStorage() {
        storageTable.show();
    }
}
