import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryIO;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.annotation.SendMessage;
import com.oocourse.library3.annotation.Trigger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class Library {
    private BookShelf bookShelf;
    private TreasuredBookShelf treasuredBookShelf;
    private AppointmentOffice appointmentOffice;
    private BorrowReturnOffice borrowReturnOffice;
    private ReadingRoom readingRoom;
    private ArrayList<LibraryReqCmd> orderQueue; // 整理时统一处理所有预约

    private HashMap<String, User> users; // K: uid, V:user
    private HashMap<LibraryBookId, Book> books; // K: Book.fullId, V: Book
    private HashMap<LibraryBookIsbn, int[]> gradeBoard; // K: ISBN, V: {grade, grade count}
    private int grade; // no use
    private boolean hasAppointment; // no use

    private static class BookInfo {
        private Book book;
        private LibraryBookState from;

        public BookInfo(Book book, LibraryBookState from) {
            this.book = book;
            this.from = from;
        }
    }

    public Library(Map<LibraryBookIsbn, Integer> bookList) {
        this.bookShelf = new BookShelf();
        this.treasuredBookShelf = new TreasuredBookShelf();
        this.appointmentOffice = new AppointmentOffice();
        this.borrowReturnOffice = new BorrowReturnOffice();
        this.readingRoom = new ReadingRoom();
        this.users = new HashMap<>();
        this.books = new HashMap<>();
        this.gradeBoard = new HashMap<>();
        this.orderQueue = new ArrayList<>();

        for (Map.Entry<LibraryBookIsbn, Integer> entry : bookList.entrySet()) {
            LibraryBookIsbn isbn = entry.getKey();
            int cnt =  entry.getValue();
            for (int i = 1; i <= cnt; i++) {
                Book book = new Book(isbn, i);
                bookShelf.addBook(book);
                books.put(book.getFullId(), book);
            }
        }
    }

    private User getOrAddUser(String uid) {
        if (users.containsKey(uid)) {
            return users.get(uid);
        }
        User user = new User(uid);
        users.put(uid, user);
        return user;
    }

    private int getIsbnGrade(LibraryBookIsbn isbn) {
        int[] info = gradeBoard.get(isbn);
        if (info == null || info[1] == 0) { // 初始为 0 分
            return 0;
        }
        return (info[0] / info[1]);
    }

    /**
     * AO，RR，BRO 收书
     * @param date 日期
     * @param isNight 晚上则计算信用分
     * @return List
     */
    private List<BookInfo> collectBooks(LocalDate date, boolean isNight) {
        List<BookInfo> pool = new ArrayList<>();
        // 信用分计算
        for (User u : users.values()) {
            if (isNight && u.getReadingBook() != null) {
                u.addCredit(-10);
            }
            for (Book b : u.getAllBorrowedBooks()) {
                if (b.getDdl() != null) {
                    boolean isOverdue = isNight ? !date.isBefore(b.getDdl()) :
                                                  date.isAfter(b.getDdl());
                    if (isOverdue) {
                        u.addCredit(-15);
                        b.clearDdl();
                    }
                }
            }
        }
        // AO，BRO，RR 收书
        // AO
        for (Order order : appointmentOffice.clearExpired(date)) {
            pool.add(new BookInfo(order.getBook(), LibraryBookState.APPOINTMENT_OFFICE));
            User user = users.get(order.getUid());
            if (user != null) {
                user.addCredit(-15);
                user.clearOrder();
            }
        }
        // BRO
        for (Book book : borrowReturnOffice.moveToBookShelf()) {
            pool.add(new BookInfo(book, LibraryBookState.BORROW_RETURN_OFFICE));
        }
        // RR
        for (Book book : readingRoom.clearAll()) {
            pool.add(new BookInfo(book, LibraryBookState.READING_ROOM));
        }
        for (User u : users.values()) {
            u.clearRead(); // 闭馆后所有人读书状态清空
        }
        for (Book book : bookShelf.getAllBooks()) {
            pool.add(new BookInfo(book, LibraryBookState.BOOKSHELF));
        }
        for (Book book : treasuredBookShelf.getAllBooks()) {
            pool.add(new BookInfo(book, LibraryBookState.TREASURED_BOOKSHELF));
        }
        return pool;
    }

    public void open(LocalDate date) {
        doArrange(date, false);
    }

    public void close(LocalDate date) {
        doArrange(date, true);
    }

    @Trigger(from = "APPOINTMENT_OFFICE", to = {"BOOKSHELF", "TREASURED_BOOKSHELF"})
    @Trigger(from = "BORROW_RETURN_OFFICE", to = {"BOOKSHELF", "TREASURED_BOOKSHELF"})
    @Trigger(from = "READING_ROOM", to = {"BOOKSHELF", "TREASURED_BOOKSHELF"})
    @Trigger(from = "BOOKSHELF", to = {"TREASURED_BOOKSHELF", "APPOINTMENT_OFFICE"})
    @Trigger(from = "TREASURED_BOOKSHELF", to = {"BOOKSHELF", "APPOINTMENT_OFFICE"})
    public void doArrange(LocalDate date, boolean isNight) { // 开馆前，闭馆后整理
        List<BookInfo> pool = collectBooks(date, isNight);
        ArrayList<LibraryMoveInfo> moves = new ArrayList<>(); // 当次整理的全部移动
        // AO/BRO/RR -> BS/TBS，一次移动
        for (BookInfo bookInfo : pool) {
            Book book = bookInfo.book;
            int grade = getIsbnGrade(book.getIsbn());
            LibraryBookState to = (grade >= 4) ? LibraryBookState.TREASURED_BOOKSHELF :
                                                 LibraryBookState.BOOKSHELF;
            if (to ==  LibraryBookState.TREASURED_BOOKSHELF) {
                treasuredBookShelf.addBook(book);
            }
            else {
                bookShelf.addBook(book);
            }
            if (bookInfo.from != to) {
                book.addMovement(date, bookInfo.from, to);
                moves.add(new LibraryMoveInfo(book.getFullId(), bookInfo.from, to));
            }
        }
        // BS/TBS -> AO，不计次数
        Iterator<LibraryReqCmd> iterator = orderQueue.iterator();
        while (iterator.hasNext()) {
            LibraryReqCmd req = iterator.next();
            LibraryBookIsbn isbn = req.getBookIsbn();
            // BS
            Book book = bookShelf.getBookForBorrow(isbn);
            LibraryBookState fromShelf = LibraryBookState.BOOKSHELF;
            if (book == null) {
                book = treasuredBookShelf.getBookForBorrow(isbn);
                fromShelf = LibraryBookState.TREASURED_BOOKSHELF;
            }
            if (book != null) {
                LocalDate expireDate = isNight ? date.plusDays(5) : date.plusDays(4);
                Order newOrder = new Order(req.getStudentId(), book, expireDate);
                appointmentOffice.addOrder(newOrder);
                book.addMovement(date, fromShelf, LibraryBookState.APPOINTMENT_OFFICE);
                moves.add(new LibraryMoveInfo(book.getFullId(), fromShelf,
                        LibraryBookState.APPOINTMENT_OFFICE, req.getStudentId()));
                iterator.remove();
            }
        }
        LibraryIO.PRINTER.move(date, moves);
    }

    // "[YYYY-mm-dd] <学号> borrowed <类别号-序列号>"
    @Trigger(from = "BOOKSHELF", to = "USER")
    @Trigger(from = "TREASURED_BOOKSHELF", to = "USER")
    public void doBorrow(LibraryReqCmd req, LocalDate date) {
        User user = getOrAddUser(req.getStudentId());
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (!user.canBorrow(isbn)) {
            LibraryIO.PRINTER.reject(req);
            return;
        }
        Book book = bookShelf.getBookForBorrow(isbn);
        LibraryBookState fromShelf = LibraryBookState.BOOKSHELF;
        if (book == null) {
            book = treasuredBookShelf.getBookForBorrow(isbn);
            fromShelf = LibraryBookState.TREASURED_BOOKSHELF;
        }
        if (book != null) {
            user.borrowBook(book);
            book.addMovement(date, fromShelf, LibraryBookState.USER);
            LocalDate ddl = date.plusDays(isbn.isTypeB() ? 15 : 30); // 设置期限
            book.setDdl(ddl);
            LibraryIO.PRINTER.accept(req, book.getFullId());
        }
        else {
            LibraryIO.PRINTER.reject(req);
        }
    }

    // "[YYYY-mm-dd] <学号> returned <类别号-序列号-副本号>"
    @Trigger(from = "USER", to = "BORROW_RETURN_OFFICE")
    public void doReturn(LibraryReqCmd req, LocalDate date) {
        User user = getOrAddUser(req.getStudentId());
        LibraryBookId bookId = req.getBookId();
        Book book = user.returnBook(bookId);
        if (book != null) {
            book.addMovement(date, LibraryBookState.USER, LibraryBookState.BORROW_RETURN_OFFICE);
            // bro 加一本书
            borrowReturnOffice.receiveBook(book);
            // 判断是否逾期
            boolean isOverdue;
            if (book.getDdl() != null) { // 在 doArrange 时未清空，表明未逾期
                isOverdue = false;
                user.addCredit(10);
                book.clearDdl();
            } else {
                isOverdue = true;
            }
            LibraryIO.PRINTER.accept(req, isOverdue ? "overdue" : "not overdue");
        }
    }

    @SendMessage(from = "Library", to = "User")
    public void orderNewBook(LibraryReqCmd req, LocalDate date) {
        this.doOrder(req, date);
    }

    // "[YYYY-mm-dd] <学号> ordered <类别号-序列号>"
    public void doOrder(LibraryReqCmd req, LocalDate date) {
        User user = getOrAddUser(req.getStudentId());
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (!user.canOrder(isbn)) {
            LibraryIO.PRINTER.reject(req);
            return;
        }
        user.orderBook(isbn);
        orderQueue.add(req);
        LibraryIO.PRINTER.accept(req);
    }

    // "[YYYY-mm-dd] <学号> picked <类别号-序列号>"
    @Trigger(from = "APPOINTMENT_OFFICE", to = "USER")
    @SendMessage(from = "Library", to = "AppointmentOffice")
    @SendMessage(from = "Library", to = "User")
    public void doPick(LibraryReqCmd req, LocalDate date) {
        User user = getOrAddUser(req.getStudentId());
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (user.getActiveOrder() == null || !user.getActiveOrder().equals(isbn)) {
            LibraryIO.PRINTER.reject(req);
            return;
        }
        Book book = appointmentOffice.pickBook(user.getUid(), isbn);
        if (book != null) { // 书在预约处
            user.getOrderedBook(book);
            user.clearOrder();
            book.addMovement(date, LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.USER);
            LocalDate ddl = date.plusDays(isbn.isTypeB() ? 15 : 30);
            book.setDdl(ddl);
            LibraryIO.PRINTER.accept(req, book.getFullId());
        }
        else { // 书还没到或已过期
            LibraryIO.PRINTER.reject(req);
        }
    }

    // "[YYYY-mm-dd] <学号> graded <类别号-序列号> <分数>"
    public void doGrade(LibraryReqCmd req, LocalDate date) {
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (!gradeBoard.containsKey(isbn)) {
            gradeBoard.put(isbn, new int[]{0, 0});
        }
        int[] info =  gradeBoard.get(isbn);
        info[0] += req.getScore();
        info[1]++;
        LibraryIO.PRINTER.accept(req);
    }

    // "[YYYY-mm-dd] <学号> read <类别号-序列号>"
    @Trigger(from = "BOOKSHELF", to = "READING_ROOM")
    @Trigger(from = "TREASURED_BOOKSHELF", to = "READING_ROOM")
    public void doRead(LibraryReqCmd req, LocalDate date) {
        User user = getOrAddUser(req.getStudentId());
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (!user.canRead(isbn)) {
            LibraryIO.PRINTER.reject(req);
            return;
        }
        Book book = bookShelf.getBookForBorrow(isbn);
        LibraryBookState from = LibraryBookState.BOOKSHELF;
        if (book == null) {
            book = treasuredBookShelf.getBookForBorrow(isbn);
            from = LibraryBookState.TREASURED_BOOKSHELF;
        }

        if (book != null) {
            user.startRead(book);
            readingRoom.receiveBook(book);
            book.addMovement(date, from, LibraryBookState.READING_ROOM);
            // "[YYYY-mm-dd] [accept] <学号> read <类别号-序列号-副本号>"
            LibraryIO.PRINTER.accept(req, book.getFullId());
        }
        else { // book == null
            LibraryIO.PRINTER.reject(req);
        }
    }

    // "[YYYY-mm-dd] <学号> restored <类别号-序列号-副本号>"
    @Trigger(from = "READING_ROOM", to = "BORROW_RETURN_OFFICE")
    public void doRestore(LibraryReqCmd req, LocalDate date) {
        User user = getOrAddUser(req.getStudentId());
        Book readingBook = user.getReadingBook();
        if (readingBook != null && readingBook.getFullId().equals(req.getBookId())) {
            user.clearRead();
            readingRoom.removeBook(readingBook);
            borrowReturnOffice.receiveBook(readingBook);
            readingBook.addMovement(date, LibraryBookState.READING_ROOM,
                                LibraryBookState.BORROW_RETURN_OFFICE);
            user.addCredit(10);
            LibraryIO.PRINTER.accept(req);
        }
        // should not go to here
        else {
            LibraryIO.PRINTER.reject(req);
        }
    }

    // "[YYYY-mm-dd] <学号> renewed <类别号-序列号-副本号>"
    public void doRenew(LibraryReqCmd req, LocalDate date) {
        User user = getOrAddUser(req.getStudentId());
        LibraryBookId bookId = req.getBookId();
        Book book = user.getBorrowedBook(bookId);
        // 未逾期则可以续订
        if (book != null && book.getDdl() != null && !date.isAfter(book.getDdl())) {
            book.renew();
            LibraryIO.PRINTER.accept(req);
        }
        else {
            LibraryIO.PRINTER.reject(req);
        }
    }

    public void queryTrace(LibraryBookId bookId, LocalDate date) {
        Book book = books.get(bookId);
        if (book != null) {
            LibraryIO.PRINTER.info(date, bookId, book.getTrace());
        }
    }

    // "[YYYY-mm-dd] <学号> queried credit score"
    public void queryCredit(LibraryQcsCmd qcs) {
        User user = getOrAddUser(qcs.getStudentId());
        int credit = user.getCredit();
        LibraryIO.PRINTER.info(qcs, credit);
    }
}
