
package phoenix.AM_PM.domain.projectplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phoenix.AM_PM.domain.projectplan.entity.ProjectPlan;

import java.util.List;
import java.util.Optional;

public interface ProjectPlanRepository extends JpaRepository<ProjectPlan, Integer> {
    boolean existsByProjectIdAndTitle(int projectId, String title);

    int countByProjectIdAndTitleStartingWith(int projectId, String titleStartsWith);

    List<ProjectPlan> findByProjectIdAndTitleStartingWith(int projectId, String titleStartsWith);

    List<ProjectPlan> findByProjectId(int projectId);

    Optional<ProjectPlan> findByProjectIdAndTitle(int projectId, String title);

    Optional<ProjectPlan> findByIdAndProjectId(int id, int projectId);

    Optional<ProjectPlan> findByProjectIdAndId(int projectId, int id);

    Optional<ProjectPlan> findByTitleAndProjectId(String title, int projectId);
    List<ProjectPlan> findByTitle(String title);

}

