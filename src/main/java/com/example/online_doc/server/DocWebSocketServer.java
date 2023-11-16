package com.example.online_doc.server;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    /**
     * 当一个新的 WebSocket 连接打开时，这个方法被调用
     * 新的会话被添加到 sessions 集合中
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    /**
     * 当一个 WebSocket 连接关闭时，这个方法被调用
     * 关闭的会话从 sessions 集合中移除
     * 同时，如果关闭的会话关联了一个用户名，这个用户名会从 editingUsers 集合中移除，并广播当前编辑用户的信息
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        String username = (String) session.getUserProperties().get("username");
        if (username != null) {
            editingUsers.remove(username);
            broadcastEditingUsers();
        }
    }

    /**
     * 当服务器收到客户端发送的消息时，这个方法被调用。消息被解析，并根据消息类型执行不同的操作：
     *
     * connect: 处理新用户连接，更新用户名，并广播文档当前内容
     * update: 更新文档内容，并广播给所有用户
     * start-editing 和 stop-editing: 更新正在编辑文档的用户列表，并广播给所有用户
     * getUser: 广播当前正在编辑文档的用户
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) throws JsonProcessingException {
        Gson gson = new Gson();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(message, Map.class);
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
     * 广播消息给所有连接的 WebSocket 会话
     * 遍历 sessions 集合，向每个会话发送指定的消息
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
     * 广播当前正在编辑文档的用户列表
     */
    private void broadcastEditingUsers() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("type", "editing");
        param.put("editingUsers", new ArrayList<>(editingUsers));

        broadcast(JSON.toJSONString(param));
    }
}
