public class Main {
    public static void main(String[] args) {
        System.out.println("---The simulation begins---");

        //先考虑各种员工只有一位
        Receptionist receptionist = new Receptionist(1);
        Dispatcher dispatcher = new Dispatcher(2);
        Courier courier = new Courier(3);
        receptionist.setDispatcher(dispatcher);
        dispatcher.setCourier(courier);

        // 开始服务
        receptionist.serve();

        System.out.println("---The simulation ends---");
    }
}
