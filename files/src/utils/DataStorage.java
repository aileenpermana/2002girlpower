package utils;

import java.util.List;

/**
 * Interface for storing and retrieving data
 * Demonstrates Dependency Inversion Principle by allowing high-level modules
 * to depend on abstraction rather than concrete implementations
 * 
 * @param <T> the type of data stored
 */
public interface DataStorage<T> {
    /**
     * Read all items from storage
     * @return list of all items
     */
    List<T> readAll();
    
    /**
     * Save a new item to storage
     * @param item the item to save
     * @return true if successful, false otherwise
     */
    boolean save(T item);
    
    /**
     * Update an existing item in storage
     * @param item the updated item
     * @return true if successful, false otherwise
     */
    boolean update(T item);
    
    /**
     * Delete an item from storage
     * @param item the item to delete
     * @return true if successful, false otherwise
     */
    boolean delete(T item);
    
    /**
     * Save all items to storage, replacing existing content
     * @param items the items to save
     * @return true if successful, false otherwise
     */
    boolean saveAll(List<T> items);
}