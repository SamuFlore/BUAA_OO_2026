import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryIO;
import com.oocourse.library1.LibraryReqCmd;
import com.oocourse.library1.LibraryMoveInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class Library {
    private BookShelf bookShelf;
    private AppointmentOffice appointmentOffice;
    private BorrowReturnOffice borrowReturnOffice;
    private ArrayList<LibraryReqCmd> orderQueue; // 整理时统一处理所有预约

    private HashMap<String, User> users; // K: uid, V:user
    private HashMap<LibraryBookId, Book> books; // K: Book.fullId, V: Book

    public Library(Map<LibraryBookIsbn, Integer> bookList) {
        this.bookShelf = new BookShelf();
        this.appointmentOffice = new AppointmentOffice();
        this.borrowReturnOffice = new BorrowReturnOffice();
        this.users = new HashMap<>();
        this.books = new HashMap<>();
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

    public void open(LocalDate date) {
        doArrange(date, false);
    }

    public void close(LocalDate date) {
        doArrange(date, true);
    }

    public void doArrange(LocalDate date, boolean isNight) { // 开馆前后整理
        ArrayList<LibraryMoveInfo> moves = new ArrayList<>();

        // AO -> BS
        List<Order> expiredOrders = appointmentOffice.clearExpired(date);
        for (Order order : expiredOrders) {
            Book book = order.getBook();
            bookShelf.addBook(book);
            moves.add(new LibraryMoveInfo(book.getFullId(), LibraryBookState.APPOINTMENT_OFFICE,
                                            LibraryBookState.BOOKSHELF));
            book.addMovement(date, LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.BOOKSHELF);
            User user = users.get(order.getUid());
            if (user != null) {
                user.clearOrder();
            }
        }
        // BRO -> BS
        List<Book> returnedBooks = borrowReturnOffice.moveToBookShelf();
        for (Book book : returnedBooks) {
            bookShelf.addBook(book);
            moves.add(new LibraryMoveInfo(book.getFullId(), LibraryBookState.BORROW_RETURN_OFFICE,
                                            LibraryBookState.BOOKSHELF));
            book.addMovement(date, LibraryBookState.BORROW_RETURN_OFFICE,
                                    LibraryBookState.BOOKSHELF);
        }
        // BS -> AO 不计移动次数
        Iterator<LibraryReqCmd> iterator = orderQueue.iterator();
        while (iterator.hasNext()) {
            LibraryReqCmd req = iterator.next();
            LibraryBookIsbn isbn = req.getBookIsbn();
            Book book = bookShelf.getBookForBorrow(isbn);
            if (book != null) {
                LocalDate expireDate;
                if (isNight) {
                    expireDate = date.plusDays(5);
                }
                else {
                    expireDate = date.plusDays(4);
                }
                Order newOrder = new Order(req.getStudentId(), book, expireDate);
                appointmentOffice.addOrder(newOrder);
                moves.add(new LibraryMoveInfo(book.getFullId(), LibraryBookState.BOOKSHELF,
                        LibraryBookState.APPOINTMENT_OFFICE, req.getStudentId()));
                book.addMovement(date, LibraryBookState.BOOKSHELF,
                                        LibraryBookState.APPOINTMENT_OFFICE);
                iterator.remove();
            }
        }
        LibraryIO.PRINTER.move(date, moves);
    }

    // "[YYYY-mm-dd] <学号> borrowed <类别号-序列号>"
    public void doBorrow(LibraryReqCmd req, LocalDate date) {
        User user = getOrAddUser(req.getStudentId());
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (!user.canBorrow(isbn)) {
            LibraryIO.PRINTER.reject(req);
            return;
        }
        Book book = bookShelf.getBookForBorrow(isbn);
        if (book != null) {
            user.borrowBook(book);
            book.addMovement(date, LibraryBookState.BOOKSHELF, LibraryBookState.USER);
            LibraryIO.PRINTER.accept(req, book.getFullId());
        }
        else {
            LibraryIO.PRINTER.reject(req);
        }
    }

    // "[YYYY-mm-dd] <学号> returned <类别号-序列号-副本号>"
    public void doReturn(LibraryReqCmd req, LocalDate date) {
        User user = getOrAddUser(req.getStudentId());
        LibraryBookId bookId = req.getBookId();
        Book book = user.returnBook(bookId);
        if (book != null) {
            book.addMovement(date, LibraryBookState.USER, LibraryBookState.BORROW_RETURN_OFFICE);
            // bro 加一本书
            borrowReturnOffice.receiveBook(book);
            LibraryIO.PRINTER.accept(req);
        }
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
    public void doPick(LibraryReqCmd req, LocalDate date) {
        User user = getOrAddUser(req.getStudentId());
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (user.getActiveOrder() == null || !user.getActiveOrder().equals(isbn) ||
            !user.canBorrow(isbn)) {
            LibraryIO.PRINTER.reject(req);
            return;
        }
        Book book = appointmentOffice.pickBook(user.getUid(), isbn);
        if (book != null) { // 书在预约处
            user.borrowBook(book);
            user.clearOrder();
            book.addMovement(date, LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.USER);
            LibraryIO.PRINTER.accept(req, book.getFullId());
        }
        else { // 书还没到或已过期
            LibraryIO.PRINTER.reject(req);
        }
    }

    public void queryTrace(LibraryBookId bookId, LocalDate date) {
        Book book = books.get(bookId);
        if (book != null) {
            LibraryIO.PRINTER.info(date, bookId, book.getTrace());
        }
    }
}
