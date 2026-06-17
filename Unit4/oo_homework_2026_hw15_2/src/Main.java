import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryOpenCmd;
import com.oocourse.library3.LibraryCloseCmd;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryQcsCmd;

import java.time.LocalDate;
import java.util.Map;

import static com.oocourse.library3.LibraryIO.SCANNER;

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
            }
            else if (command instanceof LibraryCloseCmd) {
                // 在闭馆时做点什么
                lib.close(today);
            }
            else if (command instanceof LibraryQcsCmd) {
                // 信用积分查询
                lib.queryCredit((LibraryQcsCmd) command);
            }
            else {
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
                        lib.orderNewBook(req, today);
                        break;
                    }
                    case PICKED: {
                        lib.doPick(req, today);
                        break;
                    }
                    case READ: {
                        lib.doRead(req, today);
                        break;
                    }
                    case RESTORED: {
                        lib.doRestore(req, today);
                        break;
                    }
                    case GRADED: {
                        lib.doGrade(req, today);
                        break;
                    }
                    case RENEWED: {
                        lib.doRenew(req, today);
                        break;
                    }
                    default: System.out.println("Unknown command");
                }
            }
        }
    }
}