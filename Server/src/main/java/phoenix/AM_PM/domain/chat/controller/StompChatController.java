package phoenix.AM_PM.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import phoenix.AM_PM.domain.chat.dto.RequestChat;
import phoenix.AM_PM.domain.chat.dto.ResponseChat;
import phoenix.AM_PM.domain.chat.service.StompChatService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StompChatController {
    private final SimpMessagingTemplate template;
    private final StompChatService stompChatService;

    @MessageMapping(value = "/chat/message")
    public void message(@RequestBody RequestChat requestChat) {
        ResponseChat responseChat = stompChatService.sendChat(requestChat);
        template.convertAndSend("/sub/chat/" + requestChat.getProjectId(), responseChat);
    }

    @MessageMapping(value = "/chat/enter")
    public void enter(@RequestBody RequestChat requestChat) {
        List<ResponseChat> responseChatList = stompChatService.readChat(requestChat.getProjectId(), requestChat.getUserId());
        template.convertAndSend("/sub/load/" + requestChat.getProjectId(), responseChatList);
    }
}
