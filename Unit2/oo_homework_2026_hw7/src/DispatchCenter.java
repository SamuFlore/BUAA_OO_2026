import com.oocourse.elevator3.MaintRequest;
import com.oocourse.elevator3.RecycleRequest;
import com.oocourse.elevator3.UpdateRequest;

import java.util.ArrayList;

// 将乘客分到不同的电梯等待队列
public class DispatchCenter {
    // 6 个队列，输入线程先把乘客放在此处，再分发给电梯线程
    private ArrayList<RequestQueue> elevatorQueues;
    private ArrayList<ElevatorInfo> elevatorInfos;
    private int waitingCnt = 0; // 总共等待人数
    private boolean isInputEnd = false;
    private Shaft[] shafts;
    private ElevatorThread[] elevators = new  ElevatorThread[13];

    public DispatchCenter() {
        this.elevatorQueues = new ArrayList<>();
        this.elevatorInfos = new ArrayList<>();
        this.shafts = new Shaft[7];
        for (int i = 1; i <= 6; i++) {
            shafts[i] = new Shaft(i);
        }
        // 6 台电梯，6 台备用
        int elevatorNum = 12;
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
            for (int i = 0; i < 12; i++) {
                RequestQueue queue = elevatorQueues.get(i);
                if (!queue.isActive()) { continue; }
                ElevatorInfo info = elevatorInfos.get(i);
                int curMinFloor = info.getMinFloor();
                int curMaxFloor = info.getMaxFloor();
                if (fromFloor < curMinFloor || fromFloor > curMaxFloor) {
                    continue; // 请求超出范围
                }
                if (fromFloor == curMaxFloor && toFloor > curMaxFloor) { continue; }
                if (fromFloor == curMinFloor && toFloor < curMinFloor) { continue; }
                activeCnt++;
                int curPeopleCnt = info.getPeopleCnt();
                int curReqSize = queue.getReqSize();
                int cost = calCost(info, queue, fromFloor, toFloor, curMinFloor, curMaxFloor);
                // System.out.printf("[%d]: %d\n", i + 1, cost); // 测试cost
                if (cost < minCost) {
                    minCost = cost;
                    elevId = i + 1;
                    load = curPeopleCnt + curReqSize;
                }
            }
            if (elevId != -1) {
                if (load >= 6 && activeCnt <= 6) { elevId = -1; }
                else { break; }
            }
            try { wait(100); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        Method.prtReceive(req.getPersonId(), elevId); // 输出
        elevatorQueues.get(elevId - 1).addRequest(req);
        // elevatorQueues.get(0).addRequest(req); // 测试
    }

    private int calCost(ElevatorInfo info, RequestQueue queue, int fromFloor, int toFloor,
                        int curMinFloor, int curMaxFloor) {
        int cost = 0;
        int exactTo = toFloor;
        boolean unreachable = false;
        if (exactTo > curMaxFloor) {
            exactTo = curMaxFloor;
            unreachable = true;
        }
        if (exactTo < curMinFloor) {
            exactTo = curMinFloor;
            unreachable = true;
        }
        //System.out.println(exactTo);
        if (unreachable) { cost += 114514; }
        int curFloor = info.getCurFloor();
        int exactReqDir = (exactTo > fromFloor) ? 1 : -1;
        // s = 15 * (0.3r(T_run) + 0.3r(T_avg) + 0.4r(W))
        int distance = Math.abs(curFloor - fromFloor);
        int curDir = info.getDirection();
        cost += distance * 10;
        boolean isSameDir = (curDir == 1 && fromFloor >= curFloor) ||
                (curDir == -1 && fromFloor <= curFloor);
        boolean willGoBack = false;
        //System.out.println(isSameDir);
        if (isSameDir && curDir != exactReqDir) {
            int furthest = info.getFurthest();
            if (curDir == 1 && fromFloor >=  furthest) { willGoBack = true; }
            else if (curDir == -1 && fromFloor <= furthest) { willGoBack = true; }
        }
        if (curDir != 0) {
            if ((isSameDir && curDir == exactReqDir) || willGoBack) {
                cost -= 15;
            }
            else if (!isSameDir) {
                cost += 60;
            }
        }
        //System.out.println(willGoBack);
        int curPeopleCnt = info.getPeopleCnt();
        int curReqSize = queue.getReqSize();
        cost += (curReqSize + curPeopleCnt) * 5;
        return cost;
    }

    public void dispatchMaint(MaintRequest req) { // 直接给到电梯
        elevatorQueues.get(req.getElevatorId() - 1).setMaint(req);
    }

    public void dispatchUpdate(UpdateRequest req) {
        elevatorQueues.get(req.getElevatorId() - 1).setUpdate(req);
    }

    public void dispatchRecycle(RecycleRequest req) {
        elevatorQueues.get(req.getElevatorId() - 1).setRecycle(req);
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

    public Shaft getShaft(int shaftId) {
        return this.shafts[shaftId];
    }

    public void addElevator(int elevId, ElevatorThread elevThread) {
        elevators[elevId] = elevThread;
    }

    public ElevatorThread getElevator(int elevId) {
        return elevators[elevId];
    }
}
