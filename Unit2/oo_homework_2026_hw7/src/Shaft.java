public class Shaft {
    private final int id;
    private boolean isF2invalid = false; // F2 有电梯为true
    private int idInF2 = -1;
    private int idToF2 = -1;

    public Shaft(int id) { this.id = id; }

    public synchronized void gotoF2(int elevId, DispatchCenter dspCenter) {
        this.idToF2 = elevId;
        notifyAll();

        if (isF2invalid && idInF2 != idToF2) {
            // 如果 main 在 F2 挂起，叫醒
            dspCenter.getElevatorQueue(idInF2).wakeUpForYield();
        }

        while (isF2invalid && idInF2 != idToF2) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isF2invalid = true;
        this.idInF2 = elevId;
        this.idToF2 = -1;
    }

    public synchronized void releaseF2(int elevId) {
        if (isF2invalid && idInF2 == elevId) {
            this.isF2invalid = false;
            this.idInF2 = -1;
            notifyAll();
        }
    }

    public synchronized boolean checkF2Req(int elevId) {
        return (isF2invalid && idInF2 == elevId && idToF2 != -1 && idToF2 != elevId);
    }
}
