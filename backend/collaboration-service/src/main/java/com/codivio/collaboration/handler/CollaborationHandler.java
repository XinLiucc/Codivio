package com.codivio.collaboration.handler;

import com.codivio.collaboration.room.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CollaborationHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CollaborationHandler.class);

    private final RoomManager roomManager;

    // session -> roomId
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();

    public CollaborationHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String roomId = extractRoomId(session);
        sessionRoomMap.put(session.getId(), roomId);
        roomManager.join(roomId, session);
        log.info("Connected: session={}, room={}", session.getId(), roomId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        String roomId = sessionRoomMap.get(session.getId());
        if (roomId == null) return;

        // 广播给房间内其他所有人（排除发送者）
        for (WebSocketSession other : roomManager.getSessions(roomId)) {
            if (other.isOpen() && !other.getId().equals(session.getId())) {
                if (message instanceof BinaryMessage) {
                    other.sendMessage(message);
                } else if (message instanceof TextMessage) {
                    other.sendMessage(message);
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error: session={}, error={}", session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String roomId = sessionRoomMap.remove(session.getId());
        if (roomId != null) {
            roomManager.leave(roomId, session);
        }
        log.info("Disconnected: session={}, room={}", session.getId(), roomId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String extractRoomId(WebSocketSession session) {
        // URI: /collaboration/ws/{projectId}/{fileId}
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        // parts: ["", "collaboration", "ws", "{projectId}", "{fileId}"]
        if (parts.length >= 5) {
            return roomManager.getRoomId(parts[3], parts[4]);
        }
        return "default";
    }
}
