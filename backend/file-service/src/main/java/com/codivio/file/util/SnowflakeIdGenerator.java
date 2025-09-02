package com.codivio.file.util;

import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 * 用于生成唯一的文件ID
 */
@Component
public class SnowflakeIdGenerator {

    // 起始时间戳 (2023-01-01 00:00:00)
    private final long START_TIME = 1672531200000L;

    // 机器ID位数
    private final long MACHINE_ID_BITS = 5L;
    // 数据中心ID位数
    private final long DATACENTER_ID_BITS = 5L;
    // 序列号位数
    private final long SEQUENCE_BITS = 12L;

    // 机器ID最大值
    private final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    // 数据中心ID最大值
    private final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    // 机器ID偏移量
    private final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    // 数据中心ID偏移量
    private final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    // 时间戳偏移量
    private final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATACENTER_ID_BITS;

    // 序列号掩码
    private final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final long machineId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator() {
        this(1, 1);
    }

    public SnowflakeIdGenerator(long machineId, long datacenterId) {
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException(String.format("machine Id can't be greater than %d or less than 0", MAX_MACHINE_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        this.machineId = machineId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIME) << TIMESTAMP_SHIFT) |
                (datacenterId << DATACENTER_ID_SHIFT) |
                (machineId << MACHINE_ID_SHIFT) |
                sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 生成字符串格式的ID
     * @return 字符串ID
     */
    public String nextIdString() {
        return String.valueOf(nextId());
    }
}