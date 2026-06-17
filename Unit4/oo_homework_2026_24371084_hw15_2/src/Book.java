import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryTrace;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Book {
    private LibraryBookIsbn isbn;
    private LibraryBookId fullId;
    private List<LibraryTrace> trace;
    private LocalDate ddl;

    public Book(LibraryBookIsbn isbn, int num) {
        this.isbn = isbn;
        this.fullId = new LibraryBookId(isbn.getType(), isbn.getUid(), String.format("%02d", num));
        this.trace = new ArrayList<>();
    }

    public LibraryBookIsbn getIsbn() { return isbn; }

    public LibraryBookId getFullId() { return fullId; }

    public void addMovement(LocalDate date, LibraryBookState from, LibraryBookState to) {
        this.trace.add(new LibraryTrace(date, from, to));
    }

    public List<LibraryTrace> getTrace() { return trace; }

    public void setDdl(LocalDate ddl) { this.ddl = ddl; }

    public LocalDate getDdl() { return ddl; }

    /**
     * 续订，DDL 增加 7 天
     */
    public void renew() {
        if (this.ddl != null) {
            this.ddl = this.ddl.plusDays(7);
        }
    }

    public void clearDdl() {
        this.ddl = null;
    }
}
