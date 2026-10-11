package phoenix.AM_PM.global.config.oauth;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import phoenix.AM_PM.domain.user.entity.User;
import phoenix.AM_PM.domain.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class Oauth2UserCustomService extends DefaultOAuth2UserService {
  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    System.out.println("Oauth LoadUser");
    OAuth2User user = super.loadUser(userRequest);
    saveOrUpdate(user);
    return user;
  }

  private User saveOrUpdate(OAuth2User oAuth2User) {
    Map<String, Object> attributes = oAuth2User.getAttributes();
    String email = (String) attributes.get("email");
    String name = (String) attributes.get("name");

    User user= userRepository.findByEmail(email)
//        .map(entity -> entity.update(name))
        .orElse(User.builder()
            .userId(name)
            .password("default")
            .email(email)
            .nickname(name)
            .roles("ROLE_USER")
            .build());

    return userRepository.save(user);
  }
}
