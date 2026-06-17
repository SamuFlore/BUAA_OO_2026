public class Courier {
    private Integer workerId;

    public Courier(Integer id) {
        this.workerId = id;
    }

    public Integer getWorkerId() {
        return workerId;
    }

    public void sendPackage(int packageId) {
        System.out.println("Courier " + getWorkerId() +
                " : start sending package " + packageId);
    }
}
