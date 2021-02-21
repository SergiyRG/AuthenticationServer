package org.example.app.handlers;

public interface CachedHandler {

    boolean add(String key, Object value, int secondLife);
    boolean contains(String key);
    boolean set(String key, Object value, int secondLife);
    boolean delete(String key);

    Object get(String key);
}
