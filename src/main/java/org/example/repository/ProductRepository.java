package org.example.repository;

import org.example.model.Product;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Thread-safe product repository using ReentrantReadWriteLock for concurrent access.
 * Provides CRUD operations with proper synchronization for multi-client environment.
 */
public class ProductRepository {

    private final Map<Long, Product> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    /**
     * Creates a new product with auto-generated ID.
     * Uses write lock for thread-safe insertion.
     */
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

    /**
     * Reads a product by ID.
     * Uses read lock for concurrent read access.
     */
    public Optional<Product> read(Long id) {
        readLock.lock();
        try {
            return Optional.ofNullable(storage.get(id));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Reads all products sorted by ID.
     * Uses read lock for concurrent read access.
     */
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

    /**
     * Updates an existing product.
     * Uses write lock for thread-safe modification.
     */
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

    /**
     * Deletes a product by ID and renumbers remaining products.
     * Uses write lock for thread-safe deletion.
     */
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

    /**
     * Renumbers all products sequentially after deletion.
     * Must be called while holding write lock.
     */
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

    /**
     * Checks if repository is empty.
     */
    public boolean isEmpty() {
        readLock.lock();
        try {
            return storage.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the count of products in repository.
     */
    public int count() {
        readLock.lock();
        try {
            return storage.size();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Clears all products from repository.
     */
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
