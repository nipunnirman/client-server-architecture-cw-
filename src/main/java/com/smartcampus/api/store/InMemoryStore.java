package com.smartcampus.api.store;

import com.smartcampus.api.model.Room;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryStore {
    private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();

    private InMemoryStore() {
    }

    public static Map<String, Room> rooms() {
        return ROOMS;
    }
}
