package com.univer.bookcom.cache;

public class CacheEntry<T> {
    private final T value;
    private final long timestamp;

    public CacheEntry(T value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    public T getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}