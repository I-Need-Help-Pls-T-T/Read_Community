package com.univer.bookcom.service;

import com.univer.bookcom.model.Book;
import java.util.List;

/**.
 *
 */

public interface BookService {
    /**.
     *
     */
    List<Book> searchBooks(String name, String author);
    /**.
     *
     */

    Book getBookById(int id);
    /**.
     *
     */

    void addBook(Book book);
}