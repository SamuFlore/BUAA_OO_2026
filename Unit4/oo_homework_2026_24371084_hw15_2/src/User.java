import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {
    private String uid;
    private Book bookB; // B 类一本
    private HashMap<LibraryBookIsbn, Book> bookC; // C 类一种 ISBN 一本
    private LibraryBookIsbn activeOrder; // 已生效的预约
    private Book readingBook;
    private int credit;

    public User(String uid) {
        this.uid = uid;
        bookC = new HashMap<>();
        bookB = null;
        activeOrder = null;
        readingBook = null;
        credit = 100;
    }

    public String getUid() {
        return uid;
    }

    public int getCredit() {
        return credit;
    }

    /**
     * 借阅期限内还书（包括借书和取书成功后的还书）立即+10分。</br>
     * 阅读后当日主动归还（即输入该用户的归还指令）立即+10分。</br>
     * 逾期还书：在该书借阅期限的最后一日结束后，若用户仍未还书，该用户信用积分立即-15。</br>
     * 阅读之后不还：当日闭馆后立即-10分。</br>
     * 预约后不取：用户每次预约成功，且图书送到预约处后，若用户在规定时间内未能取走该书，则当日闭馆后立即-15分。
     * @param credit 信用分
     */
    public void addCredit(int credit) {
        this.credit += credit;
        if (this.credit >= 180) {
            this.credit = 180;
        }
        else if (this.credit <= 0) {
            this.credit = 0;
        }
    }

    public boolean canBorrow(LibraryBookIsbn isbn) {
        if (credit <= 80) { return false; }
        if (isbn.isTypeA()) {
            return false;
        }
        else if (isbn.isTypeB()) {
            return bookB == null;
        }
        else if (isbn.isTypeC()) {
            return !bookC.containsKey(isbn);
        }
        return false;
    }

    public boolean canOrder(LibraryBookIsbn isbn) {
        if (credit <= 80) { return false; }
        if (isbn.isTypeA()) {
            return false;
        }
        else if (activeOrder != null) {
            return false;
        }
        else if (isbn.isTypeB() && bookB != null) {
            return false; // 若已经持有一本 B 类书，则无法再预约任何 B 类书。
        }
        else if (isbn.isTypeC() && bookC.containsKey(isbn)) {
            return false; // 若已经持有某ISBN号的 C 类书，则不能预约该ISBN号的
        }
        return true;
    }

    public boolean canRead(LibraryBookIsbn isbn) {
        if (isbn.isTypeA() && credit <= 40) { return false; }
        if ((isbn.isTypeB() || isbn.isTypeC()) && credit <= 0) {
            return false;
        }
        return readingBook == null;
    }

    public void startRead(Book book) {
        this.readingBook = book;
    }

    public void clearRead() {
        this.readingBook = null;
    }

    public Book getReadingBook() {
        return readingBook;
    }

    public void borrowBook(Book book) {
        if (book.getIsbn().isTypeB()) {
            this.bookB = book;
        }
        else if (book.getIsbn().isTypeC()) {
            this.bookC.put(book.getIsbn(), book);
        }
    }

    public Book returnBook(LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            Book bk = bookB;
            bookB = null;
            return bk;
        }
        else if (bookId.isTypeC()) {
            return bookC.remove(bookId.getBookIsbn());
        }
        return null;
    }

    public void orderBook(LibraryBookIsbn isbn) {
        this.activeOrder = isbn;
    }

    public void clearOrder() {
        this.activeOrder = null;
    }

    public LibraryBookIsbn getActiveOrder() {
        return activeOrder;
    }

    /**
     * 获取所有借阅的书，用于整理时判断是否逾期。</br>
     * B：15 天，C：30 天</br>
     * 过期扣 15 分，主动还加 10 分
     * @return List
     */
    public List<Book> getAllBorrowedBooks() {
        List<Book> books = new ArrayList<>();
        if (bookB != null) {
            books.add(bookB);
        }
        books.addAll(bookC.values());
        return books;
    }

    /**
     * 续订用，获得 Book 并调用其 renew 方法
     * @param bookId ISBN+副本号
     * @return Book
     */
    public Book getBorrowedBook(LibraryBookId bookId) {
        if (bookId.isTypeB() && bookB != null && bookB.getFullId().equals(bookId)) {
            return bookB;
        }
        else if (bookId.isTypeC() && bookC.containsKey(bookId.getBookIsbn())) {
            Book bk =  bookC.get(bookId.getBookIsbn());
            if (bk.getFullId().equals(bookId)) {
                return bk;
            }
        }
        return null;
    }

    public void getOrderedBook(Book book) {
        this.borrowBook(book);
    }
}
