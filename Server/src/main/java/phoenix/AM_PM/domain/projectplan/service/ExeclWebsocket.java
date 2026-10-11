package phoenix.AM_PM.domain.projectplan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.*;

@ServerEndpoint("/websocket")
public class ExeclWebsocket {

    static List<Session> sessionList = Collections.synchronizedList(new ArrayList<>());

    @OnOpen
    public void handleOpen(Session userSession){
        System.out.println("웹 소켓 연결");
        sessionList.add(userSession);
    }

    @OnMessage
    public void handleMessage(String message, Session userSession) throws IOException {
        System.out.println("수신된 메시지: " + message);

        // JSON 문자열을 Map으로 변환
        Map<String,Object> messageMap = new ObjectMapper().readValue(message, Map.class);

        // 세션리스트에게 데이터를 보낸다.
        for (Session session : sessionList) {
            if (session.isOpen()) {
                // JSON 문자열로 변환하여 보낸다.
                String jsonMessage = new ObjectMapper().writeValueAsString(messageMap);
                session.getBasicRemote().sendText(jsonMessage);
            }
        }
    }

    @OnClose
    public void handleClose(Session userSession){
        sessionList.remove(userSession);
    }

    @OnError
    public void handleError(Throwable t){
        t.printStackTrace();
    }
}
