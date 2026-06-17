import com.oocourse.elevator3.MaintRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

// 消费者
public class ElevatorThread implements Runnable {
    private final int id; // 1 ~ 6
    private final int maxWeight;
    private int curFloor; // 初始：F1层，范围：地下B4-B1层，地上F1-F7层，共11层
    private int curWeight; // 总重量：额定400kg
    private int direction; // 1: UP 0: WAIT -1: DOWN
    private int minFloor;
    private int maxFloor;
    private final boolean isBackup;
    private final Shaft shaft;
    private boolean isHide; // 是否在暗箱
    private MaintState maintState = MaintState.NORMAL;
    private int interval = 400; // 0.4s/层，Test 阶段变 0.2s/层
    private final LinkedList<MyPersonReq> people; // 已经在电梯里的乘客
    private final RequestQueue reqQueue; // 等待队列
    private final DispatchCenter dspCenter;
    private final ElevatorInfo elevatorInfo;
    private int allowStopCnt = 0; // 检修/升级/回收途中允许下客次数

    private enum MaintState { NORMAL, REP_ACCEPT, REPAIR, TEST, UP_ACCEPT, UPDATE, DOUBLE,
                                REC_ACCEPT, RECYCLE } // 状态机

    public ElevatorThread(int id, RequestQueue requestQueue, DispatchCenter dc, ElevatorInfo info,
                          boolean isBackup, Shaft shaft) {
        this.id = id;
        this.maxWeight = 400;
        this.reqQueue = requestQueue;
        this.people = new LinkedList<>();
        this.curFloor = 0; // 0 ~ 6, -1 ~ -4
        this.curWeight = 0;
        this.dspCenter = dc;
        this.elevatorInfo = info;
        this.isBackup = isBackup;
        this.shaft = shaft;
        this.minFloor = -4;
        this.maxFloor = 6;
        if (isBackup) {
            this.isHide = true;
            this.maintState = MaintState.TEST;
            this.reqQueue.setActive(false); // 初始时不能被分配
        }
        else {
            this.isHide = false;
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                while (isHide) {
                    if (reqQueue.isEnd() && reqQueue.isEmpty()) { return; }
                    try { wait(200); } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            elevatorInfo.update(curFloor, direction, people.size(), calFurthest());
            if (maintState == MaintState.NORMAL && reqQueue.getMaint() != null) {
                maintState = MaintState.REP_ACCEPT;
                dspCenter.setElevInactive(id); // 停止接受新的分派
                allowStopCnt = calAllowStopCnt();
            }
            else if (maintState == MaintState.NORMAL && reqQueue.getUpdate() != null) {
                maintState = MaintState.UP_ACCEPT;
                dspCenter.setElevInactive(id);
                double toF3Time = Math.abs(curFloor - 2) * 0.4;
                double cntDown = 6.0 - 0.5 - (toF3Time + 0.4 + 1);
                allowStopCnt = Math.max(0, (int)(cntDown / 0.4));
            }
            else if (maintState == MaintState.DOUBLE && reqQueue.getRecycle() != null) {
                maintState = MaintState.REC_ACCEPT;
                dspCenter.setElevInactive(id);
                double toF1time = Math.abs(curFloor) * 0.4;
                double cntDown = 6.0 - 0.5 - (toF1time + 0.4 + 1);
                allowStopCnt = Math.max(0, (int)(cntDown / 0.4));
            }
            // 状态转移
            if (maintState == MaintState.NORMAL || maintState == MaintState.DOUBLE) {
                State act = ElevatorCtrl.getAction(curFloor, direction,
                                                    curWeight, people, reqQueue,
                                                    minFloor, maxFloor);
                if (act == State.WAIT) {
                    if (curFloor == 1 &&
                            (maintState == MaintState.DOUBLE || isBackup) &&
                            reqQueue.isNeedYield()) {
                        reqQueue.clearYield();
                        move(!isBackup ? State.UP : State.DOWN); // main 则上一楼，backup 则下一楼
                        continue;
                    }
                    // 如果没人，结束线程
                    if (people.isEmpty() && reqQueue.isEmpty() && reqQueue.isEnd()) { break; }
                    this.direction = 0;
                    reqQueue.waitForReq(); // 否则等待
                }
                else if (act == State.UP || act == State.DOWN) { move(act); }
                else if (act == State.OPEN) { openAndClose(); }
            }
            else if (maintState == MaintState.REP_ACCEPT) { rep_accept(); }
            else if (maintState == MaintState.REPAIR) { repair(); }
            else if  (maintState == MaintState.TEST) { test(); }
            else if (maintState == MaintState.UP_ACCEPT) { up_accept(); }
            else if (maintState == MaintState.UPDATE) { update(); }
            else if (maintState == MaintState.REC_ACCEPT) { rec_accept(); }
            else if (maintState == MaintState.RECYCLE) { recycle(); }
        }
    }

    private synchronized int calAllowStopCnt() {
        int ans;
        MaintRequest mtReq = reqQueue.getMaint();
        int testTarget = ElevatorCtrl.getFloorNum(mtReq.getToFloor());
        double maintTravelTime = Math.abs(testTarget) * 2 * 0.2; // F1 去目标层，再回到 F1
        double fixedTime = 0.4 + 1.0 + maintTravelTime + 0.4; // 开门，维修工进，维修，去目标，回F1，开门
        double toF1Time = Math.abs(curFloor) * 0.4; // 接到维修申请前往 F1 耗时
        double cntDown = 7.0 - 0.5 - (toF1Time + fixedTime);
        ans = Math.max(0, (int)(cntDown / 0.4));
        return ans;
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
        int nextFloor = (state == State.UP) ? curFloor + 1 : curFloor - 1;
        if (nextFloor > maxFloor || nextFloor < minFloor) { return; }
        boolean needF2 = (maintState == MaintState.DOUBLE || isBackup); // 双轿厢需要检验 F2
        if (needF2 && nextFloor == 1) { // 电梯是双轿厢，并且下一层要去 F2，请求 F2
            shaft.gotoF2(this.id, dspCenter);
        }
        Method.sleepExact(interval);
        this.direction = (state == State.UP) ? 1 : -1;
        int prevFloor = this.curFloor;
        this.curFloor = nextFloor;
        Method.prtArrive(getCurFloor(), id);
        // 若离开 F2，释放 F2
        if (prevFloor == 1 && curFloor != 1 && needF2) {
            shaft.releaseF2(this.id);
            reqQueue.clearYield();
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
        ArrayList<MyPersonReq> reDispatch = new ArrayList<>();
        // 下客
        Iterator<MyPersonReq> iterator = people.iterator();
        while (iterator.hasNext()) {
            MyPersonReq person = iterator.next();
            int target = ElevatorCtrl.getFloorNum(person.getToFloor());
            boolean isArrived = person.getToFloor().equals(getCurFloor());
            // 换乘：未到站，且目标超出界限，且已经到界限
            boolean needTrans = !isArrived &&
                                (target > maxFloor || target < minFloor) &&
                                (curFloor == maxFloor || curFloor == minFloor);
            if (isArrived || needTrans) {
                if (isArrived) {
                    Method.prtOutS(person.getPersonId(), getCurFloor(), this.id);
                    dspCenter.rmWaitingPerson();
                }
                else {
                    Method.prtOutF(person.getPersonId(), getCurFloor(), this.id);
                    MyPersonReq newReq = new MyPersonReq(person.getPersonReq(), getCurFloor());
                    reDispatch.add(newReq);
                }
                curWeight -= person.getWeight();
                iterator.remove();
            }
        }
        // 统一重分配
        if (!reDispatch.isEmpty()) {
            new Thread(() -> {
                for (MyPersonReq person : reDispatch) { dspCenter.dispatch(person); }
            }).start();
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
            if (target > maxFloor) { target = maxFloor; }
            if (target < minFloor) { target = minFloor; }
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
        moveToAndKick(0);
        Method.prtOpen("F1", id);
        Method.sleepExact(400);
        ArrayList<MyPersonReq> reDispatch = new ArrayList<>();
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
                reDispatch.add(newReq);
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
        reDispatch.addAll(reqQueue.getAndClearRequests());
        // for (MyPersonReq person : waitingPeople) { dspCenter.dispatch(person); }
        if (!reDispatch.isEmpty()) {
            new Thread(() -> {
                for (MyPersonReq person : reDispatch) { dspCenter.dispatch(person); }
            }).start();
        }
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

    private void up_accept() {
        moveToAndKick(2);
        ArrayList<MyPersonReq> reDispatch = new ArrayList<>();
        // 到 F3 没人不开门
        if (!people.isEmpty()) {
            Method.prtOpen("F3", id);
            Method.sleepExact(400);
            // 剩下的全部在 1 楼下
            for (MyPersonReq person : people) {
                if (person.getToFloor().equals("F3")) {
                    Method.prtOutS(person.getPersonId(), getCurFloor(), id);
                    dspCenter.rmWaitingPerson();
                }
                else {
                    Method.prtOutF(person.getPersonId(), getCurFloor(), id);
                    MyPersonReq newReq = new MyPersonReq(person.getPersonReq(), "F3");
                    // dspCenter.dispatch(newReq);
                    reDispatch.add(newReq);
                }
            }
            people.clear();
            curWeight = 0;
            Method.prtClose("F3", id);
        }
        Method.prtUpdateBegin(id);
        maintState = MaintState.UPDATE;
        reDispatch.addAll(reqQueue.getAndClearRequests());
        if (!reDispatch.isEmpty()) {
            new Thread(() -> {
                for (MyPersonReq person : reDispatch) { dspCenter.dispatch(person); }
            }).start();
        }
    }

    private void update() {
        Method.sleepExact(1000);
        Method.prtUpdateEnd(id);
        reqQueue.clearUpdate();
        // 成为主轿厢
        this.minFloor = 1;
        this.maxFloor = 6;
        elevatorInfo.setRange(1, 6);
        this.maintState = MaintState.DOUBLE;
        dspCenter.setElevActive(id);
        // 唤醒备用
        ElevatorThread backup = dspCenter.getElevator(this.id + 6);
        backup.wakeUpFromHide();
    }

    private void rec_accept() {
        moveToAndKick(0);
        ArrayList<MyPersonReq> reDispatch = new ArrayList<>();
        // 到 F1 没人不开门
        if (!people.isEmpty()) {
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
                    reDispatch.add(newReq);
                }
            }
            people.clear();
            curWeight = 0;
            Method.prtClose("F1", id);
        }
        Method.prtRecycleBegin(id);
        maintState = MaintState.RECYCLE;
        reDispatch.addAll(reqQueue.getAndClearRequests());
        if (!reDispatch.isEmpty()) {
            new Thread(() -> {
                for (MyPersonReq person : reDispatch) { dspCenter.dispatch(person); }
            }).start();
        }
    }

    private void recycle() {
        Method.sleepExact(1000);
        Method.prtRecycleEnd(id);
        reqQueue.clearRecycle();
        // 回到暗室
        this.maintState = MaintState.TEST;
        this.isHide = true;
        ElevatorThread main = dspCenter.getElevator(this.id - 6);
        main.restoreToNormal();
    }

    // 备用轿厢启动
    public synchronized void wakeUpFromHide() {
        this.isHide = false;
        this.maintState = MaintState.DOUBLE;
        this.minFloor = -4;
        this.maxFloor = 1;
        this.elevatorInfo.setRange(-4, 1); // B4 - F2
        this.dspCenter.setElevActive(id);
        notifyAll();
    }

    // 回收
    public synchronized void restoreToNormal() {
        this.minFloor = -4;
        this.maxFloor = 6;
        this.elevatorInfo.setRange(-4, 6); // B4 - F7
        this.maintState = MaintState.NORMAL;
    }

    private synchronized void moveToAndKick(int targetFloorNum) {
        while (curFloor != targetFloorNum) {
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
            if (curFloor > targetFloorNum) {
                move(State.DOWN);
            }
            if (curFloor < targetFloorNum) {
                move(State.UP);
            }
        }
    }
}
