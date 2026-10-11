package phoenix.AM_PM.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import phoenix.AM_PM.domain.chat.entity.Chat;
import phoenix.AM_PM.domain.chat.repository.ChatRepository;
import phoenix.AM_PM.global.exception.BusinessLogicException;
import phoenix.AM_PM.global.exception.ExceptionCode;

//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@SpringBootTest
//public class ChatRepositoryTest {
//    @Autowired
//    ChatRepository chatRepository;
//
//    @Test
////    @Rollback(value = false)
//    public void createChat() {
//        Chat entity = new Chat().builder()
//                .message("메시지")
//                .unread(3)
//                .whoRead("1")
//                .build();
//        chatRepository.save(entity);
//        System.out.println(entity);
//    }
//
//    @Test
//    public void findByProjectId() {
//        Chat chat = chatRepository.findByProjectId(3).orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHAT_NOT_FOUND));
//        System.out.println(chat);
//    }
//}
