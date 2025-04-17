package com.univer.bookcom.cache;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.model.User;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CacheContainer {
    private static final int MAX_CACHE_SIZE = 3;

    private final Map<Long, CacheEntry<Book>> bookCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, CacheEntry<Book>> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;
            if (shouldRemove) {
                System.out.println("Удаление книги из кэша: id = " + eldest.getKey());
            }
            return shouldRemove;
        }

        @Override
        public CacheEntry<Book> put(Long key, CacheEntry<Book> value) {
            System.out.println("Добавление книги в кэш: id = " + key);
            return super.put(key, value);
        }
    };

    private final Map<Long, CacheEntry<User>> userCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, CacheEntry<User>> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;
            if (shouldRemove) {
                System.out.println("Удаление пользователя из кэша: id = " + eldest.getKey());
            }
            return shouldRemove;
        }

        @Override
        public CacheEntry<User> put(Long key, CacheEntry<User> value) {
            System.out.println("Добавление пользователя в кэш: id = " + key);
            return super.put(key, value);
        }
    };

    private final Map<Long, CacheEntry<Comments>> commentsCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, CacheEntry<Comments>> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;
            if (shouldRemove) {
                System.out.println("Удаление комментария из кэша: id = " + eldest.getKey());
            }
            return shouldRemove;
        }

        @Override
        public CacheEntry<Comments> put(Long key, CacheEntry<Comments> value) {
            System.out.println("Добавление комментария в кэш: id = " + key);
            return super.put(key, value);
        }
    };

    public Map<Long, CacheEntry<Book>> getBookCache() {
        return bookCache;
    }

    public Map<Long, CacheEntry<User>> getUserCache() {
        return userCache;
    }

    public Map<Long, CacheEntry<Comments>> getCommentsCache() {
        return commentsCache;
    }
}