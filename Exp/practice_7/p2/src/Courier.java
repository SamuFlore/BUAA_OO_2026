public class Courier extends Worker {
    private boolean isWorking;

    public Courier(Integer id) {
        super(id);
        this.isWorking = false;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void sendPackage(int packageId) {
        isWorking = true;
        System.out.println("Courier " + getWorkerId() +
                           " : start sending package " + packageId);
    }

    public boolean comeBack() {
        if (!isWorking) {
            System.out.println("Courier " + getWorkerId() + " already come back");
            return false;
        } else {
            isWorking = false;
            System.out.println("Courier " + getWorkerId() + " come back");
            return true;
        }
    }
}
