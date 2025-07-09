package com.codivio.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan("com.codivio.mapper")
public class MyBatisConfig {
    // MyBatis Plus会自动配置，这里只需要指定mapper扫描路径
}