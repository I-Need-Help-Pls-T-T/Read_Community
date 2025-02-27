package com.univer.bookcom.service.imp;

import com.univer.bookcom.exception.ResourceNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.service.BookService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**.
 *
 */
@Service
public class BookServiceImpl implements BookService {
    private final List<Book> books = new ArrayList<>();
    /**.
     *
     */

    public BookServiceImpl() {
        books.add(new Book("Властелин колец", "Дж. Р. Р. Толкин", "перевод",
                "Кистяковский и Муравьев", BookStatus.COMPLETED, 1954, 62));
        books.add(new Book("Война и мир", "Лев Толстой", "оригинал",
                null, BookStatus.COMPLETED, 1869, 50));
        books.add(new Book("Всезнающий читатель: побочная история", "Синг-Шонг", "перевод",
                "Ранобэ.рф", BookStatus.ONGOING, 2023, 88));
    }

    @Override
    public List<Book> searchBooks(String name, String author) {
        List<Book> result = new ArrayList<>();

        for (Book book : books) {
            boolean matchesName = name == null || book.getName().contains(name);
            boolean matchesAuthor = author == null || book.getAuthor().contains(author);

            if (matchesAuthor && matchesName) {
                result.add(new Book(
                        book.getName(),
                        book.getAuthor(),
                        book.getOriginal(),
                        book.getTranslator(),
                        book.getStatus(),
                        book.getYear(),
                        book.getChapters()
                ));
            }
        }

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Книга не найдена");
        }

        return result;
    }

    @Override
    public Book getBookById(int id) {
        if (id >= 0 && id < books.size()) {
            return books.get(id);
        } else {
            throw new ResourceNotFoundException("Книга с этим id не найдена");
        }
    }

    @Override
    public void addBook(Book book) {
        books.add(book);
    }
}