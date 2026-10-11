package phoenix.AM_PM.domain.members.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import phoenix.AM_PM.domain.members.entity.Members;
import phoenix.AM_PM.domain.members.entity.Roles;

import java.util.List;
import java.util.Optional;

public interface MembersRepository extends JpaRepository<Members, Integer> {
    List<Members> findAllByUserId(Integer userId);
    Optional<Members> findAllByUserIdAndProjectId(Integer userId, Integer projectId);
    List<Members> findAllByProjectId(Integer projectId);
    Optional<Members> findAllByProjectIdAndRoles(Integer projectId, Roles roles);
    Long countByProjectId(Integer projectId);

    @Query("SELECT m.user.id FROM Members m")
    List<Integer> findUserIds();
}