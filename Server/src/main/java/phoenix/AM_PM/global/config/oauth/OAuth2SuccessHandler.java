package phoenix.AM_PM.global.config.oauth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import phoenix.AM_PM.domain.refrash.entity.RefreshToken;
import phoenix.AM_PM.domain.refrash.service.RefreshTokenService;
import phoenix.AM_PM.domain.user.entity.User;
import phoenix.AM_PM.domain.user.service.UserService;
import phoenix.AM_PM.global.config.jwt.JwtProperties;
import phoenix.AM_PM.global.config.service.JwtServiceImpl;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private JwtProperties jwtProperties;
  private final UserService userService;
  private final RefreshTokenService refreshTokenService;
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response
      , Authentication authResult) throws IOException {

    OAuth2User oAuth2User = (OAuth2User) authResult.getPrincipal();
    User user = userService.findbyEmail((String) oAuth2User.getAttributes().get("email"));
    System.out.println(user);

    String jwtToken = JWT.create()
        .withSubject(user.getUserId())
        .withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.EXPIRATION_TIME))
        .withClaim("id", user.getId())
        .withClaim("userId", user.getUserId())
        .sign(Algorithm.HMAC512(JwtProperties.SECRET));

    String refreshToken = JWT.create()
        .withSubject(user.getUserId())
        .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME))
        .withClaim("id", user.getId())
        .withClaim("userId", user.getUserId())
        .sign(Algorithm.HMAC512(JwtProperties.SECRET));

    if(!refreshTokenService.check(user.getUserId()))
      refreshTokenService.save(RefreshToken.builder().userId(user.getUserId()).token(refreshToken).build());
    else
      refreshTokenService.update(user.getUserId(), refreshToken);

    String targetUrl = determineTargetUrl(request, response, JwtProperties.TOKEN_PREFIX+jwtToken, JwtProperties.TOKEN_PREFIX+refreshToken);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, String atoken, String rtoken) {

    String targetUrl = "http://localhost:5173/oauth2/redirect";

    return UriComponentsBuilder.fromUriString(targetUrl)
        .queryParam("atoken", atoken)
        .queryParam("rtoken", rtoken)
        .build().toUriString();
  }

}
