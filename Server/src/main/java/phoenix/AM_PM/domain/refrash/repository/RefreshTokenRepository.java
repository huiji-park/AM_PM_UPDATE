package phoenix.AM_PM.domain.refrash.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import phoenix.AM_PM.domain.refrash.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByUserId(String userId);

  boolean existsByUserId(String userId);

  void deleteByUserId(String userId);

  @Modifying
  @Query("UPDATE RefreshToken rt SET rt.token = :newToken WHERE rt.userId = :userId")
  void updateTokenByUserId(@Param("userId") String userId, @Param("newToken") String newToken);
}