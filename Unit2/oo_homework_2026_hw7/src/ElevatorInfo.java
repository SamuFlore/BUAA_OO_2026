public class ElevatorInfo {
    private int curFloor;
    private int direction;
    private int peopleCnt;
    private int furthest; // 要运动到的最远楼层
    private int minFloor;
    private int maxFloor;

    public ElevatorInfo() {
        this.curFloor = 0;
        this.direction = 0;
        this.peopleCnt = 0;
        this.furthest = 0;
        this.minFloor = -4;
        this.maxFloor = 6;
    }

    public synchronized void update(int curFloor, int direction, int peopleCnt, int furthest) {
        this.curFloor = curFloor;
        this.direction = direction;
        this.peopleCnt = peopleCnt;
        this.furthest = furthest;
    }

    public synchronized void setRange(int min, int max) {
        this.minFloor = min;
        this.maxFloor = max;
    }

    public synchronized int getCurFloor() { return this.curFloor; }

    public synchronized int getDirection() { return this.direction; }

    public synchronized int getPeopleCnt() { return this.peopleCnt; }

    public synchronized int getFurthest() { return this.furthest; }

    public synchronized int getMinFloor() { return this.minFloor; }

    public synchronized int getMaxFloor() { return this.maxFloor; }
}
