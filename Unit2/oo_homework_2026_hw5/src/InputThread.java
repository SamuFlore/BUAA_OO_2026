import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

// 生产者
public class InputThread implements Runnable {
    private ElevatorInput elevatorInput;
    private DispatchCenter dispatchCenter;

    public InputThread(DispatchCenter dispatchCenter,  ElevatorInput elevatorInput) {
        this.dispatchCenter = dispatchCenter;
        this.elevatorInput = elevatorInput;
    }

    @Override
    public void run() {
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                dispatchCenter.setAllEnd();
                break;
            }
            else {
                if (request instanceof PersonRequest) {
                    PersonRequest personRequest = (PersonRequest) request;
                    // 打印输出
                    Method.prtReceive(personRequest.getPersonId(), personRequest.getElevatorId());
                    dispatchCenter.dispatch(personRequest);
                }
            }
        }
    }
}
