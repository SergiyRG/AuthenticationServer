package org.example.app.repositories;

public interface Repository<T> {

    boolean contains(T object);
    boolean insert(T object);
    boolean isAuthorized(T object);
    boolean remove(T object);

}
