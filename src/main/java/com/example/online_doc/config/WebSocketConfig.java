package com.example.online_doc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 配置了使用 STOMP 协议的 WebSocket 服务器端点，
 * 并通过 SockJS 提供了向后兼容性。
 * 这允许客户端（如浏览器）通过 WebSocket 连接到 /shared 路径，并通过 STOMP 协议进行消息传递
 */
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * 用于注册 STOMP 端点。
     * STOMP（Simple Text Oriented Messaging Protocol）是一种简单的文本导向的消息传递协议，
     * 常用于 WebSocket 通信
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/shared").withSockJS();
    }

    /**
     * 该 Bean 是用于部署 WebSocket 服务器端点的
     * 当使用 Spring 的 WebSocket 支持时，这个 Bean 会自动注册使用了 @ServerEndpoint 注解的类。
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
