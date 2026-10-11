package phoenix.AM_PM.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import phoenix.AM_PM.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

  public Optional<User> findById(Integer id);
  public Optional<User> findByUserId(String user_id);

  public Optional<User> findByEmail(String email);
  List<User> findByNicknameContaining(String nickname);
}
