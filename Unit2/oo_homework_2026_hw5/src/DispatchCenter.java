import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;

// 将乘客分到不同的电梯等待队列
public class DispatchCenter {
    // 6 个队列，输入线程先把乘客放在此处，再分发给电梯线程
    private ArrayList<RequestQueue> elevatorQueues;

    public DispatchCenter() {
        this.elevatorQueues = new ArrayList<>();
        // 6 台电梯
        int elevatorNum = 6;
        for (int i = 0; i < elevatorNum; i++) {
            elevatorQueues.add(new RequestQueue());
        }
    }

    // 接受乘客请求，然后加入队列
    public void dispatch(PersonRequest req) {
        // id: 1 - 6
        int id = req.getElevatorId();
        elevatorQueues.get(id - 1).addRequest(req);
    }

    public void setAllEnd() {
        for (RequestQueue queue : elevatorQueues) { queue.setEnd(true); }
    }

    public RequestQueue getElevatorQueue(int elevatorId) {
        return elevatorQueues.get(elevatorId - 1);
    }
}
