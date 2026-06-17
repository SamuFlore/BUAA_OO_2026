import java.util.LinkedList;

// 计算策略，并返回电梯的动作
public class ElevatorCtrl {
    public static State getAction(int curFloor,
                                  int direction,
                                  int curWeight,
                                  LinkedList<MyPersonReq> people,
                                  RequestQueue reqQueue) {
        // 先下后上
        for (MyPersonReq person : people) {
            if (getFloorNum(person.getToFloor()) == curFloor) {
                return State.OPEN;
            }
        }

        synchronized (reqQueue) {
            LinkedList<MyPersonReq> newPeople = reqQueue.getRequests();
            boolean hasForward = hasRequestForward(curFloor, direction, newPeople);
            boolean willGoBack = people.isEmpty() && !hasForward;
            for (MyPersonReq person : newPeople) {
                // 首先人必须在这层楼
                if (getFloorNum(person.getFromFloor()) == curFloor) {
                    // 并且还有空位
                    if (curWeight + person.getWeight() <= 400) {
                        // 电梯待机，外面有人要进，开门
                        // 或电梯运行方向和人要去的方向一致，开门
                        // 或者前方的请求是反向的，但是电梯内没人，并且没有同向的请求
                        if (direction == 0 ||
                                isSameDirect(person, direction, curFloor) ||
                                willGoBack) {
                            // 重量限制在 ElevatorThread.openAndClose() 中判断
                            //System.out.println(direction);
                            //System.out.println(isSameDirect(person, direction, curFloor));
                            //System.out.println(willGoBack);
                            return State.OPEN;
                        }
                    }
                }
            }

            // 常规运行
            if (!people.isEmpty()) {
                int targetFloor = getFloorNum(people.get(0).getToFloor());
                return (targetFloor > curFloor) ? State.UP : State.DOWN;
            }
            if (!newPeople.isEmpty()) {
                if (direction == 0) {
                    int targetFloor = getFloorNum(newPeople.get(0).getFromFloor());
                    return (targetFloor > curFloor) ? State.UP : State.DOWN;
                }

                if (hasForward) {
                    return (direction == 1) ? State.UP : State.DOWN;
                }
                else { // 同方向没有，就在反方向
                    return (direction == 1) ? State.DOWN : State.UP;
                }
            }
        }
        // 彻底没活了
        return State.WAIT;
    }

    public static int getFloorNum(String curFloor) {
        if (curFloor.startsWith("F")) {
            return Integer.parseInt(curFloor.substring(1)) - 1;
        }
        else {
            return -Integer.parseInt(curFloor.substring(1));
        }
    }

    // 电梯和乘客要去的地方是否同向
    public static boolean isSameDirect(MyPersonReq person, int direction, int curFloor) {
        int targetFloor = getFloorNum(person.getToFloor());
        if (direction == 1 && targetFloor > curFloor) { return true; }
        if (direction == -1 && targetFloor < curFloor) { return true; }
        return false;
    }

    // 同方向检查有无请求
    public static boolean hasRequestForward(int curFloor, int direction,
                                            LinkedList<MyPersonReq> newPeople) {
        for (MyPersonReq person : newPeople) {
            int from = getFloorNum(person.getFromFloor());
            if (direction == 1 && from > curFloor) { return true; }
            if (direction == -1 && from < curFloor) { return true; }
        }
        return false;
    }
}
