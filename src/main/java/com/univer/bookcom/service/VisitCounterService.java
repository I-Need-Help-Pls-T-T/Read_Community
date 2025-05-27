package com.univer.bookcom.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {

    private final Map<String, AtomicInteger> visitCounts = new ConcurrentHashMap<>();

    public void increment(String methodName) {
        visitCounts.computeIfAbsent(methodName, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public int getCount(String methodName) {
        return visitCounts.getOrDefault(methodName, new AtomicInteger(0)).get();
    }

    public Map<String, Integer> getAllCounts() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        visitCounts.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
}