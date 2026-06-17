import com.oocourse.elevator2.TimableOutput;

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

    // 检修工从 F1 层进入轿厢中，然后电梯关门，随后程序输出部件检修开始信息 MAINT1-BEGIN
    public static void prtMaint1(int elevId) {
        TimableOutput.println(String.format("MAINT1-BEGIN-%d",  elevId));
    }

    // 验收测试开始：[时间戳]MAINT2-BEGIN-电梯ID
    public static void prtMaint2(int elevId) {
        TimableOutput.println(String.format("MAINT2-BEGIN-%d", elevId));
    }

    // 验收测试结束：[时间戳]MAINT-END-电梯ID
    public static void prtMaintEnd(int elevId) {
        TimableOutput.println(String.format("MAINT-END-%d", elevId));
    }

    // 睡觉zzz
    public static void sleepExact(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
