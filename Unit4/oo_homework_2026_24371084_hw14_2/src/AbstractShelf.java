import com.oocourse.library2.LibraryBookIsbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class AbstractShelf {
    private HashMap<LibraryBookIsbn, LinkedList<Book>> books;

    public AbstractShelf() {
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

    /**
     * 从书架中挑出一本书然后移除，javadoc 有点好用
     * @param isbn
     */
    public Book getBookForBorrow(LibraryBookIsbn isbn) {
        if (!books.containsKey(isbn) || books.get(isbn).isEmpty()) {
            return null;
        }
        return books.get(isbn).poll();
    }

    /**
     * 闭馆后，取出书架所有书重新评级
     * @return List
     */
    public List<Book> getAllBooks() { // 整理时重新评级
        List<Book> allBooks = new ArrayList<>();
        for (LinkedList<Book> bookList : books.values()) {
            allBooks.addAll(bookList);
        }
        books.clear();
        return allBooks;
    }
}
