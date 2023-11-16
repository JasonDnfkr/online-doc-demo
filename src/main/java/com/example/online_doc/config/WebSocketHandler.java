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

    private static final List<WebSocketSession> sessions = new ArrayList<>();

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

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }
}
