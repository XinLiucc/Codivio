package com.codivio.collaboration.handler;

import java.io.ByteArrayOutputStream;

/**
 * y-websocket 协议工具类
 *
 * 消息格式：[messageType: varint] [payload]
 *   messageType = 0 (sync)      → [syncType: varint] [data]
 *   messageType = 1 (awareness) → [awarenessData]
 *
 * syncType:
 *   0 = step1: 客户端发送自己的 state vector，请求服务端补全缺失内容
 *   1 = step2: 服务端回复缺失的 update 内容
 *   2 = update: 客户端发送增量编辑内容，需要广播
 */
public class YjsProtocol {

    public static final int MSG_SYNC = 0;
    public static final int MSG_AWARENESS = 1;
    // 自定义消息类型（>= 10 不与 y-websocket 标准类型冲突）
    public static final int MSG_SNAPSHOT = 10;

    public static final int SYNC_STEP1 = 0;
    public static final int SYNC_STEP2 = 1;
    public static final int SYNC_UPDATE = 2;

    public static boolean isSyncStep1(byte[] msg) {
        return msg.length >= 2 && (msg[0] & 0xFF) == MSG_SYNC && (msg[1] & 0xFF) == SYNC_STEP1;
    }

    public static boolean isSyncStep2(byte[] msg) {
        return msg.length >= 2 && (msg[0] & 0xFF) == MSG_SYNC && (msg[1] & 0xFF) == SYNC_STEP2;
    }

    public static boolean isSyncUpdate(byte[] msg) {
        return msg.length >= 2 && (msg[0] & 0xFF) == MSG_SYNC && (msg[1] & 0xFF) == SYNC_UPDATE;
    }

    public static boolean isAwareness(byte[] msg) {
        return msg.length >= 1 && (msg[0] & 0xFF) == MSG_AWARENESS;
    }

    /**
     * 从 sync step2 或 update 消息中提取 update 字节
     */
    public static byte[] extractUpdate(byte[] msg) {
        if (msg.length < 3) return null;
        int syncType = msg[1] & 0xFF;
        if (syncType != SYNC_STEP2 && syncType != SYNC_UPDATE) return null;

        long[] lenResult = readVarInt(msg, 2);
        int updateLen = (int) lenResult[0];
        int dataOffset = 2 + (int) lenResult[1];

        if (dataOffset + updateLen > msg.length) return null;
        byte[] update = new byte[updateLen];
        System.arraycopy(msg, dataOffset, update, 0, updateLen);
        return update;
    }

    /**
     * 构造 sync step1 消息（空 state vector，告诉客户端"我什么都没有，给我全量"）
     * 格式：[0=sync, 0=step1, 1=stateVector长度, 0=空Map]
     */
    public static byte[] buildSyncStep1() {
        return new byte[]{(byte) MSG_SYNC, (byte) SYNC_STEP1, 1, 0};
    }

    /**
     * 构造空的 sync step2 消息（空 Yjs update: [0, 0]）
     * 用于房间无内容时仍触发客户端的 sync 事件
     */
    public static byte[] buildEmptySyncStep2() {
        return buildSyncStep2(new byte[]{0, 0});
    }

    /**
     * 将一个 update 包装成 sync step2 消息发给客户端
     */
    public static byte[] buildSyncStep2(byte[] update) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(MSG_SYNC);
        out.write(SYNC_STEP2);
        writeVarInt(out, update.length);
        out.write(update, 0, update.length);
        return out.toByteArray();
    }

    public static boolean isSnapshot(byte[] msg) {
        return msg.length >= 1 && (msg[0] & 0xFF) == MSG_SNAPSHOT;
    }

    /**
     * 从快照消息中提取 update 字节（格式：[10, update...]）
     */
    public static byte[] extractSnapshot(byte[] msg) {
        if (msg.length < 2) return null;
        byte[] snapshot = new byte[msg.length - 1];
        System.arraycopy(msg, 1, snapshot, 0, snapshot.length);
        return snapshot;
    }

    // --- varint 编解码（lib0 格式）---

    private static long[] readVarInt(byte[] buf, int offset) {
        long value = 0;
        int shift = 0;
        int bytesRead = 0;
        while (offset + bytesRead < buf.length) {
            int b = buf[offset + bytesRead] & 0xFF;
            bytesRead++;
            value |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) break;
            shift += 7;
        }
        return new long[]{value, bytesRead};
    }

    private static void writeVarInt(ByteArrayOutputStream out, long value) {
        while (value > 0x7F) {
            out.write((int) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        out.write((int) (value & 0x7F));
    }
}
