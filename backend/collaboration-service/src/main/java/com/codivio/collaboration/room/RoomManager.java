package com.codivio.collaboration.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomManager {

    private static final Logger log = LoggerFactory.getLogger(RoomManager.class);

    // roomId -> Set<WebSocketSession>
    private final ConcurrentHashMap<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    public String getRoomId(String projectId, String fileId) {
        return projectId + ":" + fileId;
    }

    public void join(String roomId, WebSocketSession session) {
        rooms.computeIfAbsent(roomId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
             .add(session);
        log.debug("Session {} joined room {}, total: {}", session.getId(), roomId, rooms.get(roomId).size());
    }

    public void leave(String roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                rooms.remove(roomId);
                log.debug("Room {} is now empty, removed", roomId);
            } else {
                log.debug("Session {} left room {}, remaining: {}", session.getId(), roomId, sessions.size());
            }
        }
    }

    public Set<WebSocketSession> getSessions(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet());
    }
}
