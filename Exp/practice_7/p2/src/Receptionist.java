import java.util.Scanner;

public class Receptionist extends Worker {
    private Dispatcher dispatcher;

    public Receptionist(Integer id) {
        super(id);
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void serve() {
        Scanner scanner = new Scanner(System.in);
        label:
        while (scanner.hasNext()) {
            String operation = scanner.next();
            if (operation.equals("end")) {
                scanner.close();
                break;
            }
            if (operation.equals("return")) {
                int courierId = Integer.parseInt(scanner.next());
                courierComeBack(courierId);
                continue;
            }
            if (operation.equals("show")) {
                showStorage();
                continue;
            }
            int packageId = Integer.parseInt(scanner.next());
            switch (operation) {
                case "receive":
                    ask2receive(packageId);
                    break;
                case "take":
                    ask2take(packageId);
                    break;
                case "send":
                    ask2send(packageId);
                    break;
                default:
                    System.out.println("unknown request");
                    break label;
            }
        }
    }

    private void ask2take(int packageId) {
        // 检查等待队列
        boolean success = dispatcher.takePackage(packageId);
        if (!success) {
            System.out.println("Receptionist " + getWorkerId() +
                               " : failed to take package " + packageId);
        } else {
            System.out.println("Receptionist " + getWorkerId() +
                               " : take package " + packageId + " successfully");
        }
    }

    private void ask2receive(int packageId) {
        boolean success = dispatcher.receivePackage(packageId);
        if (!success) {
            System.out.println("Receptionist " + getWorkerId() +
                               " : failed to receive package " + packageId);
        } else {
            System.out.println("Receptionist " + getWorkerId() +
                               " : receive package " + packageId + " successfully");
        }
    }

    private void ask2send(int packageId) {
        dispatcher.arrangeSend(packageId);
        System.out.println("Receptionist " + getWorkerId() + " : package "
                           + packageId + " start delivery. Please wait patiently");
    }

    private void courierComeBack(int courierId) {
        dispatcher.courierComeBack(courierId);
    }

    private void showStorage() {
        dispatcher.showStorage();
    }
}
