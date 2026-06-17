import com.oocourse.elevator2.MaintRequest;

import java.util.Iterator;
import java.util.LinkedList;

// InputThread 负责加东西，ElevatorThread 负责取东西
public class RequestQueue {
    private LinkedList<MyPersonReq> requests; // 普通乘客的等待队列
    private MaintRequest maintRequest; // 维修队列
    private boolean active; // 1：可以 RECEIVE
    private boolean isEnd; // 检查输入是否结束

    public RequestQueue() {
        this.requests = new LinkedList<>();
        maintRequest = null;
        active = true;
        this.isEnd = false;
    }

    // InputThread 调用
    public synchronized void addRequest(MyPersonReq request) {
        this.requests.add(request);
        notifyAll();
    }

    // ElevatorThread 调用
    public synchronized LinkedList<MyPersonReq> getRequests() {
        return new LinkedList<>(this.requests);
    }

    public synchronized LinkedList<MyPersonReq> getAndClearRequests() {
        LinkedList<MyPersonReq> copy = new LinkedList<>(this.requests);
        this.requests.clear();
        notifyAll();
        return copy;
    }

    // 维修请求
    public synchronized void setMaint(MaintRequest mreq) {
        this.maintRequest = mreq;
        notifyAll();
    }

    public synchronized MaintRequest getMaint() { return this.maintRequest; }

    public synchronized void clearMaint() { this.maintRequest = null; }

    // 是否允许 RECEIVE
    public synchronized void setActive(boolean active) { this.active = active; }

    public synchronized boolean isActive() { return this.active; }

    public synchronized int getReqSize() { return this.requests.size(); }

    public synchronized void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
        notifyAll(); //
    }

    public synchronized boolean isEnd() {
        return this.isEnd;
    }

    public synchronized boolean isEmpty() { return this.requests.isEmpty(); }

    public synchronized void waitForReq() {
        while (requests.isEmpty() && !isEnd && maintRequest == null) { // 队列为空，且输入还没结束，且没有维修队列，挂起
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized LinkedList<MyPersonReq> getBoard(String curFloor,
                                                           int curWeight,
                                                           int maxWeight,
                                                           int direction) {
        int weight = curWeight;
        // 上客前方向，只接同向
        int curDir = direction;
        if (weight == 0 && curDir != 0) {
            boolean hasSameDir = false;
            for (MyPersonReq person : this.requests) {
                if (person.getFromFloor().equals(curFloor) &&
                        person.getWeight() + weight <= maxWeight) {
                    int fromFloor = ElevatorCtrl.getFloorNum(person.getFromFloor());
                    int toFloor = ElevatorCtrl.getFloorNum(person.getToFloor());
                    int personDir = (toFloor > fromFloor) ? 1 : -1;
                    if (personDir == curDir) {
                        hasSameDir = true;
                        break;
                    }
                }
            }
            if (!hasSameDir) {
                curDir = 0;
            }
        }

        LinkedList<MyPersonReq> board = new LinkedList<>();
        Iterator<MyPersonReq> iterator = this.requests.iterator();

        while (iterator.hasNext()) {
            MyPersonReq person = iterator.next();
            if (person.getFromFloor().equals(curFloor) &&
                    weight + person.getWeight() <= maxWeight) {

                int fromFloor = ElevatorCtrl.getFloorNum(person.getFromFloor());
                int toFloor = ElevatorCtrl.getFloorNum(person.getToFloor());
                int personDir = (toFloor > fromFloor) ? 1 : -1;
                // 电梯无人，谁都可以进
                // 进了一个后只进同向的
                if (curDir == 0 || personDir == curDir) {
                    board.add(person);
                    weight += person.getWeight();
                    iterator.remove();
                    if (curDir == 0) { curDir = personDir; }
                }
            }
        }
        return board;
    }
}
