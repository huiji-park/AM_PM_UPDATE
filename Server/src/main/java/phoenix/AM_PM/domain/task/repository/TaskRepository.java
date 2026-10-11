package phoenix.AM_PM.domain.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phoenix.AM_PM.domain.notice.entity.Notice;
import phoenix.AM_PM.domain.task.entity.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByProjectId(int projectId);
}
