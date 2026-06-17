import com.oocourse.elevator1.PersonRequest;

import java.util.Iterator;
import java.util.LinkedList;

// InputThread 负责加东西，ElevatorThread 负责取东西
public class RequestQueue {
    private LinkedList<PersonRequest> requests; // 一台电梯一个自己的等待队列
    private boolean isEnd; // 检查输入是否结束

    public RequestQueue() {
        this.requests = new LinkedList<>();
        this.isEnd = false;
    }

    // InputThread 调用
    public synchronized void addRequest(PersonRequest request) {
        this.requests.add(request);
        notifyAll();
    }

    // ElevatorThread 调用
    public synchronized LinkedList<PersonRequest> getRequests() {
        return new LinkedList<>(this.requests);
    }

    public synchronized void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
        notifyAll(); //
    }

    public synchronized boolean isEnd() {
        return this.isEnd;
    }

    public synchronized boolean isEmpty() { return this.requests.isEmpty(); }

    public synchronized void waitForReq() {
        while (requests.isEmpty() && !isEnd) { // 队列为空，且输入还没结束，挂起
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized LinkedList<PersonRequest> getBoard(String curFloor,
                                                           int curWeight,
                                                           int maxWeight,
                                                           int direction) {
        int weight = curWeight;
        // 上客前方向，只接同向
        int curDir = direction;
        if (weight == 0 && curDir != 0) {
            boolean hasSameDir = false;
            for (PersonRequest person : this.requests) {
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

        LinkedList<PersonRequest> board = new LinkedList<>();
        Iterator<PersonRequest> iterator = this.requests.iterator();

        while (iterator.hasNext()) {
            PersonRequest person = iterator.next();
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
