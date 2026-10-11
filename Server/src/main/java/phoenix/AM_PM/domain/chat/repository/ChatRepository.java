package phoenix.AM_PM.domain.chat.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import phoenix.AM_PM.domain.chat.entity.Chat;

import java.util.List;


public interface ChatRepository extends JpaRepository<Chat, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Chat> findByProjectIdOrderByCreatedDate(Integer projectId);

    void deleteAllByProjectId(Integer projectId);
}