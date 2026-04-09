package com.codivio.collaboration.room;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 文档状态持久化（Redis）—— 混合状态同步
 *
 * 两级结构：
 *   yjs:snapshot:{roomId}  快照（完整文档状态，由前端定期上传）
 *   yjs:updates:{roomId}   快照之后的增量列表
 *
 * 新用户/断线重连：先发快照（若有），再发增量，恢复效率稳定。
 * 房间清空时：两个 key 一并删除。
 */
@Component
public class DocStateStore {

    private static final String UPDATE_PREFIX   = "yjs:updates:";
    private static final String SNAPSHOT_PREFIX = "yjs:snapshot:";

    private final RedisTemplate<String, byte[]> redisTemplate;

    public DocStateStore(RedisTemplate<String, byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ---- 增量 update ----

    public void appendUpdate(String roomId, byte[] update) {
        redisTemplate.opsForList().rightPush(UPDATE_PREFIX + roomId, update);
    }

    public List<byte[]> getUpdates(String roomId) {
        List<byte[]> updates = redisTemplate.opsForList().range(UPDATE_PREFIX + roomId, 0, -1);
        return updates != null ? updates : Collections.emptyList();
    }

    // ---- 快照 ----

    public void saveSnapshot(String roomId, byte[] snapshot) {
        redisTemplate.opsForValue().set(SNAPSHOT_PREFIX + roomId, snapshot);
        // 快照已包含此前所有状态，清空增量列表
        redisTemplate.delete(UPDATE_PREFIX + roomId);
    }

    public byte[] getSnapshot(String roomId) {
        return redisTemplate.opsForValue().get(SNAPSHOT_PREFIX + roomId);
    }

    // ---- 房间销毁 ----

    public void clearRoom(String roomId) {
        redisTemplate.delete(UPDATE_PREFIX + roomId);
        redisTemplate.delete(SNAPSHOT_PREFIX + roomId);
    }
}
