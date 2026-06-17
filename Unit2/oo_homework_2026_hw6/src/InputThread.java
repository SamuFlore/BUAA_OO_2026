import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;

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
                dispatchCenter.setInputEnd();
                break;
            }
            else {
                if (request instanceof PersonRequest) {
                    PersonRequest personRequest = (PersonRequest) request;
                    dispatchCenter.addWaitingPerson(); // 等待人数 + 1
                    MyPersonReq mpr = new MyPersonReq(personRequest, personRequest.getFromFloor());
                    dispatchCenter.dispatch(mpr); // through dispatcher
                }
                else if (request instanceof MaintRequest) {
                    MaintRequest maintRequest = (MaintRequest) request;
                    dispatchCenter.dispatchMaint(maintRequest); // go to elevator directly
                }
            }
        }
    }
}
