package phoenix.AM_PM.domain.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phoenix.AM_PM.domain.project.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
}