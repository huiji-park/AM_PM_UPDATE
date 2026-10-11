package phoenix.AM_PM.domain.refrash.service;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import phoenix.AM_PM.domain.refrash.entity.RefreshToken;
import phoenix.AM_PM.domain.refrash.repository.RefreshTokenRepository;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  public Optional<RefreshToken> finduserId(String userId){
    return refreshTokenRepository.findByUserId(userId);
  }

  public boolean save(RefreshToken token){
    try {
      refreshTokenRepository.save(token);
    } catch (Exception e){
      System.out.println("Refresh 토큰 저장 실패");
      System.out.println(e);
      return false;
    }
    return true;
  }

  public boolean check(String userId){
    return refreshTokenRepository.existsByUserId(userId);
  }

  @Transactional
  public boolean update(String userId, String token){
    try {
      refreshTokenRepository.updateTokenByUserId(userId, token);
    } catch (Exception e){
      System.out.println("Refresh 토큰 수정 실패");
      System.out.println(e);
      return false;
    }
    return true;
  }

  @Transactional
  public boolean delete(String userId){
    try {
      refreshTokenRepository.deleteByUserId(userId);
    } catch (Exception e){
      System.out.println("Refresh 토큰 수정 실패");
      System.out.println(e);
      return false;
    }
    return true;
  }
}
