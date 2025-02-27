package com.univer.bookcom.controller;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.service.BookService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**.
 * Book controller
 */
@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;
    /**.
     *
     */

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    /**.
     *
     */

    @GetMapping("/search")
    public List<Book> searchBooks(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String author) {
        return bookService.searchBooks(name, author);
    }
    /**.
     *
     */

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable int id) {
        return bookService.getBookById(id);
    }
    /**.
     *
     */

    @PostMapping("/add")
    public void addBook(@RequestBody Book book) {
        bookService.addBook(book);
    }
}