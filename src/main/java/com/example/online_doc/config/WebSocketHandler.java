package com.example.online_doc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebSocketHandler extends TextWebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);

    /**
     * 定义了一个静态列表 sessions，用于存储所有建立的 WebSocket 会话
     * 允许服务器跟踪当前所有打开的 WebSocket 连接
     */
    private static final List<WebSocketSession> sessions = new ArrayList<>();

    /**
     * 当收到客户端发送的消息时，服务器会记录这条消息，并将其转发给所有连接的会话（即广播消息）
     * @param session
     * @param message
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        LOGGER.info("Received message: {}", message.getPayload());
        for (WebSocketSession webSocketSession : sessions) {
            try {
                webSocketSession.sendMessage(message);
            } catch (IOException e) {
                LOGGER.error("Error: {}", e.getMessage());
            }
        }
    }

    /**
     * 在 WebSocket 连接建立后被调用。
     * 将新的会话添加到 sessions 列表中，这样就可以跟踪当前所有打开的连接
     * @param session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    /**
     * 当 WebSocket 连接关闭时，这个方法被调用。它从 sessions 列表中移除已关闭的会话。
     * @param session
     * @param status
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }
}
