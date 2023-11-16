package com.example.online_doc.server;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

@ServerEndpoint("/shared")
@Component
@Slf4j
public class DocWebSocketServer {
    private static Set<Session> sessions = new HashSet<>();
    private static Set<String> editingUsers = new HashSet<>();
    private static String content = "";

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        String username = (String) session.getUserProperties().get("username");
        if (username != null) {
            editingUsers.remove(username);
            broadcastEditingUsers();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Gson gson = new Gson();
        Map<String, Object> data = gson.fromJson(message, Map.class);
        String type = (String) data.get("type");
        log.info("Message type: {}, message data: {}", type, data);
        String jsonStr = "";

        switch (type) {
            case "connect":
                String username = (String) data.get("username");
                session.getUserProperties().put("username", username);
                jsonStr = JSON.toJSONString(new HashMap<String, Object>() {{
                    put("type", "update");
                    put("content", content);
                }});
                broadcast(jsonStr);
                break;
            case "update":
                content = (String) data.get("content");
                jsonStr = JSON.toJSONString(new HashMap<String, Object>() {{
                    put("type", "update");
                    put("content", content);
                }});
                broadcast(jsonStr);
                break;
            case "start-editing":
                username = (String) session.getUserProperties().get("username");
                editingUsers.add(username);
                broadcastEditingUsers();
                break;
            case "stop-editing":
                username = (String) session.getUserProperties().get("username");
                editingUsers.remove(username);
                broadcastEditingUsers();
                break;
            case "getUser":
                broadcastEditingUsers();
                break;
        }
    }

    /**
     * 广播当前文本信息
     * @param message
     */
    private void broadcast(String message) {
        log.info("message   {}", message);
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 广播当前正在编辑文本的用户
     */
    private void broadcastEditingUsers() {
        broadcast(
                JSON.toJSONString(
                        new HashMap<String, Object>() {{
                            put("type", "editing");
                            put("editingUsers", new ArrayList<>(editingUsers));
                        }}));
    }
}
