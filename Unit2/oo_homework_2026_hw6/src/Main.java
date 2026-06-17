import com.oocourse.elevator2.TimableOutput;
import com.oocourse.elevator2.ElevatorInput;

public class Main {
    public static void main(String[] args) throws Exception {
        TimableOutput.initStartTimestamp(); // MUST INITIALIZE
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        DispatchCenter dispatchCenter = new DispatchCenter();

        for (int i = 1; i <= 6; i++) {
            RequestQueue reqQueue = dispatchCenter.getElevatorQueue(i);
            ElevatorInfo elevatorInfo = dispatchCenter.getElevatorInfo(i);
            ElevatorThread elevator = new ElevatorThread(i, reqQueue, dispatchCenter, elevatorInfo);
            Thread elevThread = new Thread(elevator);
            elevThread.start();
        }

        InputThread input = new InputThread(dispatchCenter, elevatorInput);
        Thread inputThread = new Thread(input);
        inputThread.start();
    }
}
