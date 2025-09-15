package com.codivio.file.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类 - 文件服务
 * 配置消息队列的消费者和生产者
 */
@Configuration
public class RabbitConfig {

    // ================================ 队列和交换机常量 ================================

    /**
     * 文件操作交换机名称
     */
    public static final String FILE_OPERATION_EXCHANGE = "codivio.file.operation.exchange";

    /**
     * 文件操作队列名称
     */
    public static final String FILE_OPERATION_QUEUE = "codivio.file.operation.queue";

    /**
     * 文件操作路由键
     */
    public static final String FILE_OPERATION_ROUTING_KEY = "file.operation";

    /**
     * 文件操作回调交换机
     */
    public static final String FILE_CALLBACK_EXCHANGE = "codivio.file.callback.exchange";

    /**
     * 文件操作回调队列
     */
    public static final String FILE_CALLBACK_QUEUE = "codivio.file.callback.queue";

    /**
     * 文件操作回调路由键
     */
    public static final String FILE_CALLBACK_ROUTING_KEY = "file.callback";

    // ================================ 消息转换器配置 ================================

    /**
     * JSON消息转换器
     * 配置Jackson支持Java 8时间类型
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册JavaTimeModule支持LocalDateTime等Java 8时间类型
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期序列化为时间戳，使用注解指定的格式
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate配置
     * 设置消息转换器和确认机制
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        
        // 开启消息确认机制
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("回调消息发送成功: " + correlationData);
            } else {
                System.err.println("回调消息发送失败: " + cause);
            }
        });

        // 开启消息返回机制
        template.setReturnsCallback((returned) -> {
            System.err.println("回调消息返回: " + returned.getMessage() + 
                              ", 交换机: " + returned.getExchange() +
                              ", 路由键: " + returned.getRoutingKey());
        });

        return template;
    }

    // ================================ 回调消息发送配置 ================================

    /**
     * 文件操作回调交换机
     * 用于向项目服务发送操作结果
     */
    @Bean
    public DirectExchange fileCallbackExchange() {
        return ExchangeBuilder
                .directExchange(FILE_CALLBACK_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 文件操作回调队列
     * 项目服务监听此队列处理回调
     */
    @Bean
    public Queue fileCallbackQueue() {
        return QueueBuilder
                .durable(FILE_CALLBACK_QUEUE)
                .build();
    }

    /**
     * 文件操作回调绑定
     */
    @Bean
    public Binding fileCallbackBinding() {
        return BindingBuilder
                .bind(fileCallbackQueue())
                .to(fileCallbackExchange())
                .with(FILE_CALLBACK_ROUTING_KEY);
    }
}