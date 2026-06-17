import com.oocourse.elevator3.TimableOutput;
import com.oocourse.elevator3.ElevatorInput;

public class Main {
    public static void main(String[] args) throws Exception {
        TimableOutput.initStartTimestamp(); // MUST INITIALIZE
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        DispatchCenter dispatchCenter = new DispatchCenter();

        for (int i = 1; i <= 12; i++) {
            RequestQueue reqQueue = dispatchCenter.getElevatorQueue(i);
            ElevatorInfo elevatorInfo = dispatchCenter.getElevatorInfo(i);
            int shaftId = (i <= 6) ? i : i - 6; // 1-7, 2-8, ...
            Shaft shaft = dispatchCenter.getShaft(shaftId);
            boolean isBackup = (i > 6);
            ElevatorThread elevator = new ElevatorThread(i, reqQueue, dispatchCenter, elevatorInfo,
                                                        isBackup, shaft);
            dispatchCenter.addElevator(i, elevator);
            Thread elevThread = new Thread(elevator);
            elevThread.start();
        }

        InputThread input = new InputThread(dispatchCenter, elevatorInput);
        Thread inputThread = new Thread(input);
        inputThread.start();
    }
}
