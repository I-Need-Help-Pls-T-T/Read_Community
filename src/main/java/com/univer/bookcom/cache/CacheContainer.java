package com.univer.bookcom.cache;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.model.User;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CacheContainer {
    private static final Logger log = LoggerFactory.getLogger(CacheContainer.class);
    private static final int MAX_CACHE_SIZE = 3;

    private final Map<Long, CacheEntry<Book>> bookCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, CacheEntry<Book>> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;
            if (shouldRemove) {
                log.info("Удаление книги из кэша: id = {}", eldest.getKey());
            }
            return shouldRemove;
        }

        @Override
        public CacheEntry<Book> put(Long key, CacheEntry<Book> value) {
            log.info("Добавление книги в кэш: id = {}", key);
            return super.put(key, value);
        }
    };

    private final Map<Long, CacheEntry<User>> userCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, CacheEntry<User>> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;
            if (shouldRemove) {
                log.info("Удаление пользователя из кэша: id = {}", eldest.getKey());
            }
            return shouldRemove;
        }

        @Override
        public CacheEntry<User> put(Long key, CacheEntry<User> value) {
            log.info("Добавление пользователя в кэш: id = {}", key);
            return super.put(key, value);
        }
    };

    private final Map<Long, CacheEntry<Comments>> commentsCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, CacheEntry<Comments>> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;
            if (shouldRemove) {
                log.info("Удаление комментария из кэша: id = {}", eldest.getKey());
            }
            return shouldRemove;
        }

        @Override
        public CacheEntry<Comments> put(Long key, CacheEntry<Comments> value) {
            log.info("Добавление комментария в кэш: id = {}", key);
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