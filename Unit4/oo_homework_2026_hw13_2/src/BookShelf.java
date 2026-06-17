import com.oocourse.library1.LibraryBookIsbn;

import java.util.HashMap;
import java.util.LinkedList;

public class BookShelf {
    private HashMap<LibraryBookIsbn, LinkedList<Book>> books;

    public BookShelf() {
        books = new HashMap<>();
    }

    public void addBook(Book book) {
        if (books.containsKey(book.getIsbn())) {
            books.get(book.getIsbn()).add(book);
        }
        else {
            LinkedList<Book> bookList = new LinkedList<>();
            bookList.add(book);
            books.put(book.getIsbn(), bookList);
        }
    }

    public Book getBookForBorrow(LibraryBookIsbn isbn) {
        if (!books.containsKey(isbn) || books.get(isbn).isEmpty()) {
            return null;
        }
        return books.get(isbn).poll();
    }
}
