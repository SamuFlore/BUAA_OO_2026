import java.util.ArrayList;

public class CourierTable {
    private ArrayList<Courier> courierTable;

    public CourierTable(int cnt) {
        courierTable = new ArrayList<>();
        for (int i = 1; i <= cnt; i++) {
            courierTable.add(new Courier(i));
        }
    }

    public Courier findSpareCourier() {
        for (Courier courier : courierTable) {
            if (!courier.isWorking()) {
                return courier;
            }
        }
        return null;
    }

    public boolean comeBack(int courierId) {
        for (Courier courier : courierTable) {
            if (courier.getWorkerId() == courierId) {
                return courier.comeBack();
            }
        }
        // 出错
        System.out.println("Courier " + courierId + " not found");
        return false;
    }
}
