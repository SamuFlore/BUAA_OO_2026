import java.time.LocalDate;

public class Order { // 预约信息
    private String uid;
    private Book book;
    private LocalDate expireDate; // 到 AO 的时间

    public Order(String uid, Book book, LocalDate expireDate) {
        this.uid = uid;
        this.book = book;
        this.expireDate = expireDate;
    }

    public boolean isExpired(LocalDate now) {
        return now.isAfter(expireDate);
    }

    public String getUid() {
        return uid;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }
}
