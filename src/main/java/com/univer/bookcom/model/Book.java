package com.univer.bookcom.model;

/**.
 *
 */

public class Book {
    private final String name;
    private final String author;
    final String original;
    final String translator;
    final int year;
    final int chapters;
    final BookStatus status;
    /**.
     *
     */

    public Book(String name, String author, String original,
                String translator, BookStatus status, int year, int chapters) {
        this.name = name;
        this.author = author;
        this.original = original;
        this.translator = translator;
        this.status = status;
        this.year = year;
        this.chapters = chapters;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getOriginal() {
        return original;
    }

    public String getTranslator() {
        return translator;
    }

    public int getYear() {
        return year;
    }

    public int getChapters() {
        return chapters;
    }

    public BookStatus getStatus() {
        return status;
    }
}
