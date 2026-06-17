public class ElevatorInfo {
    private int curFloor;
    private int direction;
    private int peopleCnt;
    private int furthest; // 要运动到的最远楼层

    public ElevatorInfo() {
        this.curFloor = 0;
        this.direction = 0;
        this.peopleCnt = 0;
        this.furthest = 0;
    }

    public synchronized void update(int curFloor, int direction, int peopleCnt, int furthest) {
        this.curFloor = curFloor;
        this.direction = direction;
        this.peopleCnt = peopleCnt;
        this.furthest = furthest;
    }

    public synchronized int getCurFloor() { return this.curFloor; }

    public synchronized int getDirection() { return this.direction; }

    public synchronized int getPeopleCnt() { return this.peopleCnt; }

    public synchronized int getFurthest() { return this.furthest; }
}
