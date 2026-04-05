package com.codivio.collaboration.handler;

import com.codivio.collaboration.room.DocStateStore;
import com.codivio.collaboration.room.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CollaborationHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CollaborationHandler.class);

    private final RoomManager roomManager;
    private final DocStateStore docStateStore;

    // sessionId -> roomId
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();

    public CollaborationHandler(RoomManager roomManager, DocStateStore docStateStore) {
        this.roomManager = roomManager;
        this.docStateStore = docStateStore;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String roomId = extractRoomId(session);
        sessionRoomMap.put(session.getId(), roomId);
        roomManager.join(roomId, session);
        log.info("Connected: session={}, room={}", session.getId(), roomId);

        // 1. 发 sync step1，触发客户端发来它的 state vector
        session.sendMessage(new BinaryMessage(YjsProtocol.buildSyncStep1()));

        // 2. 把 Redis 中已有的所有 update 逐条发给新客户端（恢复文档状态）
        List<byte[]> storedUpdates = docStateStore.getUpdates(roomId);
        for (byte[] update : storedUpdates) {
            session.sendMessage(new BinaryMessage(YjsProtocol.buildSyncStep2(update)));
        }
        log.debug("Sent {} stored updates to session {}", storedUpdates.size(), session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        if (!(message instanceof BinaryMessage)) return;

        byte[] data = ((BinaryMessage) message).getPayload().array();
        String roomId = sessionRoomMap.get(session.getId());
        if (roomId == null) return;

        if (YjsProtocol.isSyncStep1(data)) {
            // 客户端请求同步：把所有存储的 update 作为 step2 回复
            List<byte[]> storedUpdates = docStateStore.getUpdates(roomId);
            for (byte[] update : storedUpdates) {
                session.sendMessage(new BinaryMessage(YjsProtocol.buildSyncStep2(update)));
            }

        } else if (YjsProtocol.isSyncStep2(data)) {
            // 客户端回传的初始同步内容：存 Redis，不广播
            byte[] update = YjsProtocol.extractUpdate(data);
            if (update != null) {
                docStateStore.appendUpdate(roomId, update);
            }

        } else if (YjsProtocol.isSyncUpdate(data)) {
            // 客户端的增量编辑：存 Redis + 广播给房间其他人
            byte[] update = YjsProtocol.extractUpdate(data);
            if (update != null) {
                docStateStore.appendUpdate(roomId, update);
            }
            broadcast(session, roomId, message);

        } else if (YjsProtocol.isAwareness(data)) {
            // 光标/在线用户信息：只广播，不存
            broadcast(session, roomId, message);
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

    private void broadcast(WebSocketSession sender, String roomId, WebSocketMessage<?> message) {
        for (WebSocketSession other : roomManager.getSessions(roomId)) {
            if (other.isOpen() && !other.getId().equals(sender.getId())) {
                try {
                    other.sendMessage(message);
                } catch (IOException e) {
                    log.error("Failed to send to session {}: {}", other.getId(), e.getMessage());
                }
            }
        }
    }

    private String extractRoomId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        if (parts.length >= 5) {
            return roomManager.getRoomId(parts[3], parts[4]);
        }
        return "default";
    }
}
