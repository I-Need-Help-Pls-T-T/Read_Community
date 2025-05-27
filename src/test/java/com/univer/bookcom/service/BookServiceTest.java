package com.univer.bookcom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookServiceTest {

    private BookRepository bookRepository;
    private UserService userService;
    private CacheContainer cacheContainer;
    private Map<Long, CacheEntry<Book>> bookCache;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        userService = mock(UserService.class);
        cacheContainer = mock(CacheContainer.class);
        bookCache = new LinkedHashMap<>();
        when(cacheContainer.getBookCache()).thenReturn(bookCache);

        bookService = new BookService(bookRepository, userService, cacheContainer);
    }

    @Test
    void testSaveBook_addsToCache() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.saveBook(book);

        assertEquals(book, result);
        assertTrue(bookCache.containsKey(1L));
    }

    @Test
    void testGetBookById_fromCache() {
        Book book = new Book();
        book.setId(1L);
        bookCache.put(1L, new CacheEntry<>(book));

        Optional<Book> result = bookService.getBookById(1L);

        assertTrue(result.isPresent());
        assertEquals(book, result.get());
    }

    @Test
    void testGetBookById_fromRepository() {
        Book book = new Book();
        book.setId(2L);
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.getBookById(2L);

        assertTrue(result.isPresent());
        assertEquals(book, result.get());
        assertTrue(bookCache.containsKey(2L));
    }

    @Test
    void testUpdateBook_success() {
        Book book = new Book();
        book.setId(3L);
        book.setTitle("Old");

        Book updated = new Book();
        updated.setTitle("New");
        updated.setCountChapters(10);
        updated.setPublicYear(2020);
        updated.setBookStatus(BookStatus.ONGOING);

        when(bookRepository.findById(3L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.updateBook(3L, updated);

        assertEquals("New", result.getTitle());
        assertEquals(10, result.getCountChapters());
        assertEquals(2020, result.getPublicYear());
        assertEquals(BookStatus.ONGOING, result.getBookStatus());
        assertTrue(bookCache.containsKey(3L));
    }

    @Test
    void testUpdateBook_notFound_throwsException() {
        Long bookId = 1L;
        Book bookUpdateData = new Book();

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () ->
                bookService.updateBook(bookId, bookUpdateData));
    }

    @Test
    void testDeleteBook_removesFromCacheAndRepository() {
        bookCache.put(5L, new CacheEntry<>(new Book()));
        doNothing().when(bookRepository).deleteById(5L);

        bookService.deleteBook(5L);

        assertFalse(bookCache.containsKey(5L));
        verify(bookRepository).deleteById(5L);
    }

    @Test
    void testCreateBookWithAuthor_success() {
        Book book = new Book();
        book.setId(10L);
        User author = new User();
        author.setId(100L);

        when(userService.getUserById(100L)).thenReturn(Optional.of(author));
        when(bookRepository.save(any())).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        Book result = bookService.createBookWithAuthor(100L, book);

        assertEquals(10L, result.getId());
        assertTrue(result.getAuthors().contains(author));
        assertTrue(bookCache.containsKey(10L));
    }

    @Test
    void testCreateBookWithAuthor_authorNotFound_throwsException() {
        long authorId = 123L;
        Book newBook = new Book();

        when(userService.getUserById(authorId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                bookService.createBookWithAuthor(authorId, newBook));
    }

    @Test
    void testIsCachedOrExists() {
        bookCache.put(1L, new CacheEntry<>(new Book()));
        assertTrue(bookService.isCachedOrExists(1L));

        bookCache.clear();
        when(bookRepository.existsById(2L)).thenReturn(true);
        assertTrue(bookService.isCachedOrExists(2L));

        when(bookRepository.existsById(3L)).thenReturn(false);
        assertFalse(bookService.isCachedOrExists(3L));
    }

    @Test
    void testFindBooksByTitle() {
        List<Book> expected = List.of(new Book());
        when(bookRepository.findByTitle("Test")).thenReturn(expected);

        assertEquals(expected, bookService.findBooksByTitle("Test"));
    }

    @Test
    void testFindBooksByAuthorString() {
        List<Book> expected = List.of(new Book());
        when(bookRepository.findByAuthor("SomeAuthor")).thenReturn(expected);

        assertEquals(expected, bookService.findBooksByAuthor("SomeAuthor"));
    }

    @Test
    void testFindBooksByAuthorUser() {
        User author = new User();
        List<Book> expected = List.of(new Book());
        when(bookRepository.findByAuthorsContaining(author)).thenReturn(expected);

        assertEquals(expected, bookService.findBooksByAuthor(author));
    }

    @Test
    void testFindBooksByStatus() {
        List<Book> books = List.of(new Book());
        when(bookRepository.findByStatus(BookStatus.COMPLETED)).thenReturn(books);

        assertEquals(books, bookService.findBooksByStatus(BookStatus.COMPLETED));
    }

    @Test
    void testFindBooksByPublicYear() {
        List<Book> books = List.of(new Book());
        when(bookRepository.findByPublicYear(2000L)).thenReturn(books);

        assertEquals(books, bookService.findBooksByPublicYear(2000L));
    }

    @Test
    void testCacheEvictionWhenFull() {
        BookService testBookService = new BookService(bookRepository, userService, cacheContainer);

        for (int i = 1; i <= 4; i++) {
            Book book = new Book();
            book.setTitle("Book " + i);
            book.setCountChapters(i);
            book.setPublicYear(2000 + i);
            book.setBookStatus(BookStatus.ANNOUNCED);

            long bookId = i;

            when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
                Book input = invocation.getArgument(0);
                input.setId(bookId);
                return input;
            });

            testBookService.saveBook(book);
        }

        assertEquals(3, cacheContainer.getBookCache().size());
    }
}