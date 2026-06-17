import com.oocourse.elevator2.MaintRequest;

import java.util.Iterator;
import java.util.LinkedList;

// 消费者
public class ElevatorThread implements Runnable {
    private final int id; // 1 ~ 6
    private final int maxWeight;
    private int curFloor; // 初始：F1层，范围：地下B4-B1层，地上F1-F7层，共11层
    private int curWeight; // 总重量：额定400kg
    private int direction; // 1: UP 0: WAIT -1: DOWN
    private MaintState maintState = MaintState.NORMAL;
    private int interval = 400; // 0.4s/层，Test 阶段变 0.2s/层
    private final LinkedList<MyPersonReq> people; // 已经在电梯里的乘客
    private final RequestQueue reqQueue; // 等待队列
    private final DispatchCenter dspCenter;
    private final ElevatorInfo elevatorInfo;
    private int allowStopCnt = 0; // 往 F1 接检修工途中允许下客次数

    private enum MaintState { NORMAL, REP_ACCEPT, REPAIR, TEST } // 维修状态

    public ElevatorThread(int id, RequestQueue requestQueue, DispatchCenter dc, ElevatorInfo info) {
        this.id = id;
        this.maxWeight = 400;
        this.reqQueue = requestQueue;
        this.people = new LinkedList<>();
        this.curFloor = 0; // 0 ~ 6, -1 ~ -4
        this.curWeight = 0;
        this.dspCenter = dc;
        this.elevatorInfo = info;
    }

    @Override
    public void run() {
        while (true) {
            elevatorInfo.update(curFloor, direction, people.size(), calFurthest());
            if (maintState == MaintState.NORMAL && reqQueue.getMaint() != null) {
                maintState = MaintState.REP_ACCEPT;
                dspCenter.setElevInactive(id); // 停止接受新的分派
                MaintRequest mtReq = reqQueue.getMaint();
                int testTarget = ElevatorCtrl.getFloorNum(mtReq.getToFloor());
                double maintTravelTime = Math.abs(testTarget) * 2 * 0.2; // F1 去目标层，再回到 F1
                double fixedTime = 0.4 + 1.0 + maintTravelTime + 0.4; // 开门，维修工进，维修，去目标，回F1，开门
                double toF1Time = Math.abs(curFloor) * 0.4; // 接到维修申请前往 F1 耗时
                double cntDown = 7.0 - 0.5 - (toF1Time + fixedTime);
                allowStopCnt = Math.max(0, (int)(cntDown / 0.4));
            }

            if (maintState == MaintState.NORMAL) {
                State act = ElevatorCtrl.getAction(curFloor,
                                                    direction,
                                                    curWeight,
                                                    people,
                                                    reqQueue);
                if (act == State.WAIT) {
                    // 如果没人并且已结束，则退出线程
                    if (people.isEmpty() && reqQueue.isEmpty() && reqQueue.isEnd()) {
                        break;
                    }
                    this.direction = 0;
                    reqQueue.waitForReq(); // 否则等待
                }
                else if (act == State.UP || act == State.DOWN) {
                    move(act);
                }
                else if (act == State.OPEN) {
                    openAndClose();
                }
            }
            else if (maintState == MaintState.REP_ACCEPT) { rep_accept(); }
            else if (maintState == MaintState.REPAIR) { repair(); }
            else if  (maintState == MaintState.TEST) { test(); }
        }
    }

    private String getCurFloor() {
        String floor = "";
        if (curFloor >= 0) {
            floor = "F" +  (curFloor + 1);
        }
        else {
            floor = "B" + (-curFloor);
        }
        return floor;
    }

    private void move(State state) {
        try {
            Thread.sleep(interval);
            switch (state) {
                case UP: {
                    if (curFloor >= 6) { break; }
                    this.curFloor++;
                    this.direction = 1;
                    break;
                }
                case DOWN: {
                    if (curFloor <= -4) { break; }
                    this.curFloor--;
                    this.direction = -1;
                    break;
                }
                default: {
                    break;
                }
            }
            // 打印输出
            Method.prtArrive(getCurFloor(), id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void moveTo(int targetFloor) {
        while (curFloor != targetFloor) {
            if (curFloor < targetFloor) { move(State.UP); }
            if (curFloor > targetFloor) { move(State.DOWN); }
        }
    }

    private void openAndClose() {
        // 打印输出
        Method.prtOpen(getCurFloor(), id);
        // 先 sleep
        Method.sleepExact(400);
        // 下客
        Iterator<MyPersonReq> iterator = people.iterator();
        while (iterator.hasNext()) {
            MyPersonReq person = iterator.next();
            // 如果到目的地
            if (person.getToFloor().equals(getCurFloor())) {
                Method.prtOutS(person.getPersonId(), getCurFloor(), id);
                curWeight -=  person.getWeight();
                iterator.remove();
                dspCenter.rmWaitingPerson(); // 彻底完成一个请求
            }
        }
        elevatorInfo.update(curFloor, direction, people.size(), calFurthest());
        // 上客
        synchronized (this.reqQueue) {
            LinkedList<MyPersonReq> newPeople = reqQueue.getBoard(getCurFloor(),
                                                                    curWeight,
                                                                    maxWeight,
                                                                    direction);
            for (MyPersonReq person : newPeople) {
                this.people.add(person);
                this.curWeight += person.getWeight();
                Method.prtIn(person.getPersonId(), getCurFloor(), id);
            }
        }
        // 更新方向
        if (!people.isEmpty()) {
            int target = ElevatorCtrl.getFloorNum(people.get(0).getToFloor());
            this.direction = (target > curFloor) ? 1 : -1;
        }
        else { this.direction = 0; }
        Method.prtClose(getCurFloor(), id);
        elevatorInfo.update(curFloor, direction, people.size(), calFurthest());
    }

    private int calFurthest() {
        if (this.direction == 0) { return this.curFloor; }
        int furthest = this.curFloor;
        for (MyPersonReq person : people) { // 电梯内
            int target = ElevatorCtrl.getFloorNum(person.getToFloor());
            if (this.direction == 1 && target > furthest) {  furthest = target; }
            if (this.direction == -1 && target < furthest) {  furthest = target; }
        }

        LinkedList<MyPersonReq> newPeople = reqQueue.getRequests(); // 等待队列
        for (MyPersonReq person : newPeople) {
            int from =  ElevatorCtrl.getFloorNum(person.getFromFloor());
            int to = ElevatorCtrl.getFloorNum(person.getToFloor());
            if (this.direction == 1) {
                if (from > furthest) {  furthest = from; }
                if (to > from && to > furthest) {  furthest = to; }
            }
            if (this.direction == -1) {
                if (from < furthest) {  furthest = from; }
                if (to < from && to < furthest) {  furthest = to; }
            }
        }
        return furthest;
    }

    private void rep_accept() {
        if (curFloor != 0) {
            boolean hasOff = false;
            for (MyPersonReq person : people) {
                if (person.getToFloor().equals(getCurFloor())) {
                    hasOff = true;
                    break;
                }
            }
            if (hasOff && allowStopCnt > 0) { // 有人要下，而且还有时间
                Method.prtOpen(getCurFloor(), id);
                Method.sleepExact(400);
                Iterator<MyPersonReq> iterator = people.iterator();
                while (iterator.hasNext()) {
                    MyPersonReq person = iterator.next();
                    if (person.getToFloor().equals(getCurFloor())) {
                        Method.prtOutS(person.getPersonId(), getCurFloor(), id);
                        curWeight -= person.getWeight();
                        iterator.remove();
                        dspCenter.rmWaitingPerson();
                    }
                }
                Method.prtClose(getCurFloor(), id);
                elevatorInfo.update(curFloor, direction, people.size(), calFurthest());
                allowStopCnt--;
            }
            if (curFloor > 0) {
                move(State.DOWN);
                return;
            }
            if (curFloor < 0) {
                move(State.UP);
                return;
            }
        }

        Method.prtOpen("F1", id);
        Method.sleepExact(400);
        // 剩下的全部在 1 楼下
        for (MyPersonReq person : people) {
            if (person.getToFloor().equals("F1")) {
                Method.prtOutS(person.getPersonId(), getCurFloor(), id);
                dspCenter.rmWaitingPerson();
            }
            else {
                Method.prtOutF(person.getPersonId(), getCurFloor(), id);
                MyPersonReq newReq = new MyPersonReq(person.getPersonReq(), "F1");
                // dspCenter.dispatch(newReq);
                new Thread(() -> dspCenter.dispatch(newReq)).start();
            }
        }
        people.clear();
        curWeight = 0;

        // 检修工上
        MaintRequest mtReq = reqQueue.getMaint();
        Method.prtIn(mtReq.getWorkerId(), "F1", id);
        Method.prtClose("F1", id);
        Method.prtMaint1(id);
        maintState = MaintState.REPAIR;
        // 重新分配此电梯等待队列中的人
        LinkedList<MyPersonReq> waitingPeople = reqQueue.getAndClearRequests();
        // for (MyPersonReq person : waitingPeople) { dspCenter.dispatch(person); }
        new Thread(() -> {
            for (MyPersonReq person : waitingPeople) { dspCenter.dispatch(person); }
        }).start();
    }

    private void repair() {
        Method.sleepExact(1000);
        Method.prtMaint2(id);
        maintState = MaintState.TEST;
    }

    private void test() {
        this.interval = 200;
        MaintRequest mtReq = reqQueue.getMaint();
        int targetFloor = ElevatorCtrl.getFloorNum(mtReq.getToFloor());
        moveTo(targetFloor);
        moveTo(0); // 回 F1
        Method.prtOpen("F1", id);
        // 检修工下
        Method.sleepExact(400);
        Method.prtOutS(mtReq.getWorkerId(), "F1", id);
        Method.prtClose("F1", id);
        Method.prtMaintEnd(id);

        reqQueue.clearMaint();
        this.interval = 400;
        maintState = MaintState.NORMAL;

        dspCenter.setElevActive(id);
    }
}
