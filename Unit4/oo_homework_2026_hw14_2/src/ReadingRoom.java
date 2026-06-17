import java.util.ArrayList;
import java.util.List;

public class ReadingRoom {
    private List<Book> bookList;

    public ReadingRoom() {
        this.bookList = new ArrayList<>();
    }

    public void receiveBook(Book book) {
        this.bookList.add(book);
    }

    public void removeBook(Book book) {
        this.bookList.remove(book);
    }

    /**
     * 清空所有书
     * @return List
     */
    public List<Book> clearAll() {
        List<Book> res = new ArrayList<>(bookList);
        this.bookList.clear();
        return res;
    }
}
