import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryOpenCmd;
import com.oocourse.library1.LibraryCloseCmd;
import com.oocourse.library1.LibraryReqCmd;

import java.time.LocalDate;
import java.util.Map;

import static com.oocourse.library1.LibraryIO.SCANNER;

public class Main {
    public static void main(String[] args) {
        // 获取图书馆内所有书籍ISBN号及相应副本数
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();
        Library lib = new Library(bookList);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) { break; }
            LocalDate today = command.getDate(); // 今天的日期
            if (command instanceof LibraryOpenCmd) {
                // 在开馆时做点什么
                lib.open(today);
            } else if (command instanceof LibraryCloseCmd) {
                // 在闭馆时做点什么
                lib.close(today);
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                switch (req.getType()) {
                    case QUERIED: {
                        lib.queryTrace(req.getBookId(), today);
                        break;
                    }
                    case BORROWED: {
                        lib.doBorrow(req, today);
                        break;
                    }
                    case RETURNED: {
                        lib.doReturn(req, today);
                        break;
                    }
                    case ORDERED: {
                        lib.doOrder(req, today);
                        break;
                    }
                    case PICKED: {
                        lib.doPick(req, today);
                        break;
                    }
                    default: System.out.println("Unknown command");
                }
            }
        }
    }
}