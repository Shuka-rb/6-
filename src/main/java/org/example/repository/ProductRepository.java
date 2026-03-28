package org.example.repository;

import org.example.model.Product;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


public class ProductRepository {

    private final Map<Long, Product> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();


    public Product create(Product product) {
        writeLock.lock();
        try {
            Long id = idGenerator.getAndIncrement();
            product.setId(id);
            storage.put(id, product);
            return product;
        } finally {
            writeLock.unlock();
        }
    }


    public Optional<Product> read(Long id) {
        readLock.lock();
        try {
            return Optional.ofNullable(storage.get(id));
        } finally {
            readLock.unlock();
        }
    }


    public List<Product> readAll() {
        readLock.lock();
        try {
            return storage.values().stream()
                    .sorted(Comparator.comparingLong(Product::getId))
                    .collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }


    public Optional<Product> update(Long id, Product updatedProduct) {
        writeLock.lock();
        try {
            if (!storage.containsKey(id)) {
                return Optional.empty();
            }
            updatedProduct.setId(id);
            storage.put(id, updatedProduct);
            return Optional.of(updatedProduct);
        } finally {
            writeLock.unlock();
        }
    }


    public boolean delete(Long id) {
        writeLock.lock();
        try {
            if (!storage.containsKey(id)) {
                return false;
            }

            storage.remove(id);
            renumberProducts();

            return true;
        } finally {
            writeLock.unlock();
        }
    }


    private void renumberProducts() {
        List<Product> products = storage.values().stream()
                .sorted(Comparator.comparingLong(Product::getId))
                .collect(Collectors.toList());

        storage.clear();
        long newId = 1;
        for (Product product : products) {
            product.setId(newId);
            storage.put(newId, product);
            newId++;
        }

        idGenerator.set(newId);
    }


    public boolean isEmpty() {
        readLock.lock();
        try {
            return storage.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    public int count() {
        readLock.lock();
        try {
            return storage.size();
        } finally {
            readLock.unlock();
        }
    }


    public void clear() {
        writeLock.lock();
        try {
            storage.clear();
            idGenerator.set(1);
        } finally {
            writeLock.unlock();
        }
    }
}
