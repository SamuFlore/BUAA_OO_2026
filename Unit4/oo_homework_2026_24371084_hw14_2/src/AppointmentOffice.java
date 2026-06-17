import com.oocourse.library2.LibraryBookIsbn;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class AppointmentOffice {
    private List<Order> orders; // 在 AO 的待取书籍

    public AppointmentOffice() {
        orders = new ArrayList<>();
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public Book pickBook(String uid, LibraryBookIsbn isbn) {
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order.getUid().equals(uid) && order.getBook().getIsbn().equals(isbn)) {
                orders.remove(i);
                return order.getBook();
            }
        }
        return null;
    }

    public List<Order> clearExpired(LocalDate now) {
        List<Order> orderList = new ArrayList<>();
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.isExpired(now)) {
                orderList.add(order);
                iterator.remove();
            }
        }
        return orderList;
    }
}
