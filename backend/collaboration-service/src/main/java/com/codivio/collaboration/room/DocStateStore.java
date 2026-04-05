package com.codivio.collaboration.room;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 文档状态持久化（Redis）
 *
 * 每个房间维护一个 update 列表：yjs:updates:{projectId}:{fileId}
 * 新用户连接时把所有历史 update 依次发给他，即可恢复完整文档状态
 */
@Component
public class DocStateStore {

    private static final String KEY_PREFIX = "yjs:updates:";

    private final RedisTemplate<String, byte[]> redisTemplate;

    public DocStateStore(RedisTemplate<String, byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void appendUpdate(String roomId, byte[] update) {
        redisTemplate.opsForList().rightPush(KEY_PREFIX + roomId, update);
    }

    public List<byte[]> getUpdates(String roomId) {
        List<byte[]> updates = redisTemplate.opsForList().range(KEY_PREFIX + roomId, 0, -1);
        return updates != null ? updates : Collections.emptyList();
    }
}
