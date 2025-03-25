package com.service.core.websockets.repositories;

public interface SessionRepository<K, V> {

    void put(K key, V value);

    void remove(K key);

    V get(K key);

}
