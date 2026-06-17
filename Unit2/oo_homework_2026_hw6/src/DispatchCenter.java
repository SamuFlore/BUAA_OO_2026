import com.oocourse.elevator2.MaintRequest;

import java.util.ArrayList;

// 将乘客分到不同的电梯等待队列
public class DispatchCenter {
    // 6 个队列，输入线程先把乘客放在此处，再分发给电梯线程
    private ArrayList<RequestQueue> elevatorQueues;
    private ArrayList<ElevatorInfo> elevatorInfos;
    private int waitingCnt = 0; // 总共等待人数
    private boolean isInputEnd = false;

    public DispatchCenter() {
        this.elevatorQueues = new ArrayList<>();
        this.elevatorInfos = new ArrayList<>();
        // 6 台电梯
        int elevatorNum = 6;
        for (int i = 0; i < elevatorNum; i++) {
            elevatorQueues.add(new RequestQueue());
            elevatorInfos.add(new ElevatorInfo());
        }
    }

    public synchronized void addWaitingPerson() { this.waitingCnt++; }

    public synchronized void rmWaitingPerson() {
        this.waitingCnt--;
        checkEnd();
    }

    public synchronized void setInputEnd() {
        this.isInputEnd = true;
        checkEnd();
    }

    private synchronized void checkEnd() {
        if (waitingCnt == 0 && isInputEnd) { setAllEnd(); }
    }

    // 接受乘客请求，然后加入队列
    public synchronized void dispatch(MyPersonReq req) { // 需要调度算法
        int elevId = -1;
        int fromFloor = ElevatorCtrl.getFloorNum(req.getFromFloor());
        int toFloor = ElevatorCtrl.getFloorNum(req.getToFloor());
        int reqDir = (toFloor > fromFloor) ? 1 : -1;
        while (true) {
            int minCost = Integer.MAX_VALUE;
            int activeCnt = 0; // 可 RECEIVE 电梯数量
            int load = 0; // 选中的电梯等待队列长度
            for (int i = 0; i < 6; i++) {
                RequestQueue queue = elevatorQueues.get(i);
                if (!queue.isActive()) { continue; }
                activeCnt++;
                ElevatorInfo info = elevatorInfos.get(i);
                int curFloor = info.getCurFloor();
                int curDir = info.getDirection();
                int curPeopleCnt = info.getPeopleCnt();
                int curReqSize = queue.getReqSize();
                // s = 15 * (0.3r(T_run) + 0.3r(T_avg) + 0.4r(W))
                int distance = Math.abs(curFloor - fromFloor);
                int cost = distance * 10;

                boolean isSameDir = (curDir == 1 && fromFloor >= curFloor) ||
                                    (curDir == -1 && fromFloor <= curFloor);
                boolean willGoBack = false;
                //System.out.println(isSameDir);
                if (isSameDir && curDir != reqDir) {
                    int furthest = info.getFurthest();
                    if (curDir == 1 && fromFloor >=  furthest) { willGoBack = true; }
                    else if (curDir == -1 && fromFloor <= furthest) { willGoBack = true; }
                }
                if (curDir != 0) {
                    if ((isSameDir && curDir == reqDir) || willGoBack) {
                        cost -= 15;
                    }
                    else if (!isSameDir) {
                        cost += 60;
                    }
                }
                //System.out.println(willGoBack);
                cost += (curReqSize + curPeopleCnt) * 5;
                //System.out.printf("[%d]: %d\n", i + 1, cost);
                if (cost < minCost) {
                    minCost = cost;
                    elevId = i + 1;
                    load = curPeopleCnt + curReqSize;
                }
            }
            if (elevId != -1) {
                if (load >= 6 && activeCnt < 6) { elevId = -1; }
                else { break; }
            }
            try { wait(100); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        Method.prtReceive(req.getPersonId(), elevId); // 输出
        elevatorQueues.get(elevId - 1).addRequest(req);
        // elevatorQueues.get(0).addRequest(req); // 测试
    }

    public void dispatchMaint(MaintRequest req) { // 直接给到电梯
        elevatorQueues.get(req.getElevatorId() - 1).setMaint(req);
    }

    public void setAllEnd() {
        for (RequestQueue queue : elevatorQueues) { queue.setEnd(true); }
    }

    public RequestQueue getElevatorQueue(int elevatorId) {
        return elevatorQueues.get(elevatorId - 1);
    }

    public ElevatorInfo getElevatorInfo(int elevatorId) {
        return elevatorInfos.get(elevatorId - 1);
    }

    public synchronized void setElevActive(int elevId) {
        elevatorQueues.get(elevId - 1).setActive(true);
        notifyAll();
    }

    public synchronized void setElevInactive(int elevId) {
        elevatorQueues.get(elevId - 1).setActive(false);
    }
}
