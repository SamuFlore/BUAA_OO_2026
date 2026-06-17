import com.oocourse.elevator1.PersonRequest;

import java.util.Iterator;
import java.util.LinkedList;

// 消费者
public class ElevatorThread implements Runnable {
    private final int id; // 1 ~ 6
    private final int maxWeight;
    private int curFloor; // 初始：F1层，范围：地下B4-B1层，地上F1-F7层，共11层
    private int curWeight; // 总重量：额定400kg
    private int direction; // 1: UP 0: WAIT -1: DOWN
    private final LinkedList<PersonRequest> people; // 已经在电梯里的乘客
    private final RequestQueue reqQueue; // 等待队列

    public ElevatorThread(int id, RequestQueue requestQueue) {
        this.id = id;
        this.maxWeight = 400;
        this.reqQueue = requestQueue;
        this.people = new LinkedList<>();
        this.curFloor = 0; // 0 ~ 6, -1 ~ -4
        this.curWeight = 0;
    }

    @Override
    public void run() {
        while (true) {
            Action act = ElevatorCtrl.getAction(curFloor, direction, curWeight, people, reqQueue);
            if (act == Action.WAIT) {
                // 如果没人并且已结束，则退出线程
                if (people.isEmpty() && reqQueue.isEmpty() && reqQueue.isEnd()) {
                    break;
                }
                this.direction = 0;
                reqQueue.waitForReq(); // 否则等待
            }
            else if (act == Action.UP || act == Action.DOWN) {
                move(act);
            }
            else if (act == Action.OPEN) {
                openAndClose();
            }
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

    private void move(Action action) {
        try {
            Thread.sleep(400);
            switch (action) {
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

    private void openAndClose() {
        // 打印输出
        Method.prtOpen(getCurFloor(), id);
        // 先 sleep
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 下客
        Iterator<PersonRequest> iterator = people.iterator();
        while (iterator.hasNext()) {
            PersonRequest person = iterator.next();
            // 如果到目的地
            if (person.getToFloor().equals(getCurFloor())) {
                Method.prtOutS(person.getPersonId(), getCurFloor(), id);
                curWeight -=  person.getWeight();
                iterator.remove();
            }
        }
        // 上客
        synchronized (this.reqQueue) {
            LinkedList<PersonRequest> newPeople = reqQueue.getBoard(getCurFloor(),
                                                                    curWeight,
                                                                    maxWeight,
                                                                    direction);
            for (PersonRequest person : newPeople) {
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
    }
}
