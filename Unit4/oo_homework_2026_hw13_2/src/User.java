import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;

public class User {
    private String uid;
    private Book bookB; // B 类一本
    private HashMap<LibraryBookIsbn, Book> bookC; // C 类一种 ISBN 一本
    private LibraryBookIsbn activeOrder; // 已生效的预约

    public User(String uid) {
        this.uid = uid;
        bookC = new HashMap<>();
        bookB = null;
        activeOrder = null;
    }

    public String getUid() {
        return uid;
    }

    public boolean canBorrow(LibraryBookIsbn isbn) {
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
}
