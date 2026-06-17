import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryTrace;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Book {
    private LibraryBookIsbn isbn;
    private LibraryBookId fullId;
    private List<LibraryTrace> trace;

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
}
