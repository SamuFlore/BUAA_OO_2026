import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.MaintRequest;
import com.oocourse.elevator3.UpdateRequest;
import com.oocourse.elevator3.RecycleRequest;

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
                else if (request instanceof UpdateRequest) {
                    UpdateRequest updateRequest = (UpdateRequest) request;
                    dispatchCenter.dispatchUpdate(updateRequest);
                }
                else if (request instanceof RecycleRequest) {
                    RecycleRequest recycleRequest = (RecycleRequest) request;
                    dispatchCenter.dispatchRecycle(recycleRequest);
                }
            }
        }
    }
}
