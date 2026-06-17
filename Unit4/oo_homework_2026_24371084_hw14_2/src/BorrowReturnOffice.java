import java.util.ArrayList;
import java.util.List;

public class BorrowReturnOffice {
    private List<Book> tmpBooks;

    public BorrowReturnOffice() {
        tmpBooks = new ArrayList<>();
    }

    public void receiveBook(Book book) {
        tmpBooks.add(book);
    }

    /**
     * 清空所有书
     * @return List
     */
    public List<Book> moveToBookShelf() {
        List<Book> copy = new ArrayList<>(tmpBooks);
        tmpBooks.clear();
        return copy;
    }
}
