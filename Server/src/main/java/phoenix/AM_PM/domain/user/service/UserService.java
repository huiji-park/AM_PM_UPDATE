package phoenix.AM_PM.domain.user.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import phoenix.AM_PM.domain.user.dto.ResponseUser;
import phoenix.AM_PM.domain.user.dto.SaveUserDto;
import phoenix.AM_PM.domain.user.entity.User;
import phoenix.AM_PM.domain.user.repository.UserRepository;
import phoenix.AM_PM.global.exception.BusinessLogicException;
import phoenix.AM_PM.global.exception.ExceptionCode;

@RequiredArgsConstructor
@Service
public class UserService {
  private final UserRepository repository;
//  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  // user 저장/ 회원가입 service
  public boolean save(SaveUserDto user) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    boolean result = true;

    User newUser = User.builder()
        .userId(user.getUserId())
        .password(encoder.encode(user.getPassword()))
        .nickname(user.getNickname())
        .email(user.getEmail())
        .roles("ROLE_USER")
        .build();

    try {
      repository.save(newUser);
    } catch (Exception e) {
      System.out.println(e);
      result = false;
    }
    return result;
  }

  public List<User> findbynickname(String nickname) { return repository.findByNicknameContaining(nickname);}
  // userId 중복 체크
  public Optional<User> findbyUserId(String userId) {
    return repository.findByUserId(userId);
  }

  // email 중복 체크
  public User findbyEmail(String email) {

    return repository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
  }

  public ResponseUser getUserDetail(Integer userId) {
    User user = repository.findById(userId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
    return ResponseUser.from(user);
  }
}
