import com.oocourse.elevator1.TimableOutput;

// 静态方法类
public class Method {
    // [时间戳]RECEIVE-乘客ID-电梯ID
    public static void prtReceive(int psgId, int elevId) {
        TimableOutput.println(String.format("RECEIVE-%d-%d", psgId, elevId));
    }

    // [时间戳]ARRIVE-所在层-电梯ID
    public static void prtArrive(String curFloor, int elevId) {
        TimableOutput.println(String.format(("ARRIVE-%s-%d"), curFloor, elevId));
    }

    // [时间戳]OPEN-所在层-电梯ID
    public static void prtOpen(String curFloor, int elevId) {
        TimableOutput.println(String.format(("OPEN-%s-%d"), curFloor, elevId));
    }

    // [时间戳]CLOSE-所在层-电梯ID
    public static void prtClose(String curFloor, int elevId) {
        TimableOutput.println(String.format("CLOSE-%s-%d", curFloor, elevId));
    }

    // [时间戳]IN-乘客ID-所在层-电梯ID
    public static void prtIn(int psgId, String curFloor, int elevId) {
        TimableOutput.println(String.format("IN-%d-%s-%d", psgId, curFloor, elevId));
    }

    // 当前楼层是乘客目标楼层时：[时间戳]OUT-S-乘客ID-所在层-电梯ID
    public static void prtOutS(int psgId, String curFloor, int elevId) {
        TimableOutput.println(String.format("OUT-S-%d-%s-%d", psgId, curFloor, elevId));
    }

    // 当前楼层不是乘客目标楼层时：[时间戳]OUT-F-乘客ID-所在层-电梯ID
    public static void prtOutF(int psgId, String curFloor, int elevId) {
        TimableOutput.println(String.format("OUT-F-%d-%s-%d", psgId, curFloor, elevId));
    }
}
