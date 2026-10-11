package phoenix.AM_PM.domain.chat.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import phoenix.AM_PM.domain.chat.dto.RequestChat;
import phoenix.AM_PM.domain.chat.dto.ResponseChat;
import phoenix.AM_PM.domain.chat.entity.Chat;
import phoenix.AM_PM.domain.chat.repository.ChatRepository;
import phoenix.AM_PM.domain.members.repository.MembersRepository;
import phoenix.AM_PM.domain.project.repository.ProjectRepository;
import phoenix.AM_PM.domain.user.repository.UserRepository;
import phoenix.AM_PM.global.exception.BusinessLogicException;
import phoenix.AM_PM.global.exception.ExceptionCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class StompChatService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ChatRepository chatRepository;
    private final MembersRepository membersRepository;

    public StompChatService(UserRepository userRepository, ProjectRepository projectRepository, ChatRepository chatRepository, MembersRepository membersRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.chatRepository = chatRepository;
        this.membersRepository = membersRepository;
    }

    public ResponseChat sendChat(RequestChat requestChat) {
        Chat entity = new Chat().builder()
                .message(requestChat.getMessage())
                .user(userRepository.findById(requestChat.getUserId()).orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND)))
                .project(projectRepository.findById(requestChat.getProjectId()).orElseThrow(() -> new BusinessLogicException(ExceptionCode.PROJECT_NOT_FOUND)))
                .unread(membersRepository.countByProjectId(requestChat.getProjectId()) - 1)
                .whoRead(String.valueOf(requestChat.getUserId()))
                .build();
        Chat chat = chatRepository.save(entity);
        return ResponseChat.from(chat);
    }

    @Transactional
    public List<ResponseChat> readChat(Integer projectId, Integer userId) {
        List<Chat> chatList = chatRepository.findByProjectIdOrderByCreatedDate(projectId);
        List<ResponseChat> responseChatList = new ArrayList<>();
        chatList.forEach(chat -> readCheck(chat, userId));
        chatList.forEach(chat -> responseChatList.add(ResponseChat.from(chat)));
        return responseChatList;
    }

    @Transactional
    public void readCheck(Chat chat, Integer userId) {
        String[] reads = chat.getWhoRead().split(",");
        if(!Arrays.asList(reads).contains(String.valueOf(userId))) {
            chat.updateWhoRead(chat.getWhoRead() + "," + userId);
            chat.updateUnread(membersRepository.countByProjectId(chat.getProject().getId()) - (reads.length + 1));
        }
        else
            chat.updateUnread(membersRepository.countByProjectId(chat.getProject().getId()) - reads.length);
    }
}
