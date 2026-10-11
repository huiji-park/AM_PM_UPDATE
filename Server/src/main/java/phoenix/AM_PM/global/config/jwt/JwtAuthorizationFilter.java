package phoenix.AM_PM.global.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import phoenix.AM_PM.domain.refrash.repository.RefreshTokenRepository;
import phoenix.AM_PM.domain.refrash.service.RefreshTokenService;
import phoenix.AM_PM.domain.user.entity.User;
import phoenix.AM_PM.domain.user.repository.UserRepository;
import phoenix.AM_PM.global.config.auth.MyUserDetails;

// 인가
public class JwtAuthorizationFilter extends BasicAuthenticationFilter{
	
	private UserRepository userRepository;

	@Autowired
	private RefreshTokenService refreshTokenService;
	
	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
		super(authenticationManager);
		this.userRepository = userRepository;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.println(request);
		String header = request.getHeader(JwtProperties.HEADER_STRING);
		String rHeader = request.getHeader(JwtProperties.REFRESH_TOKEN_HEADER);

		if (header == null || !header.startsWith(JwtProperties.TOKEN_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}

		System.out.println("header : " + header);
		System.out.println("header 2 : " + rHeader);

		String token = header.replace(JwtProperties.TOKEN_PREFIX, "");

		if (rHeader != null && rHeader.startsWith(JwtProperties.REFRESH_TOKEN_PREFIX)) {
			System.out.println("Access Token 재발급");
			String refreshToken = rHeader.replace(JwtProperties.REFRESH_TOKEN_PREFIX, "");
			System.out.println(refreshToken);

			try {
				Algorithm algorithm = Algorithm.HMAC512(JwtProperties.SECRET);
				JWTVerifier verifier = JWT.require(algorithm).build();
				DecodedJWT decodedJWT = verifier.verify(refreshToken);

				String userId = decodedJWT.getSubject();
				User user = userRepository.findByUserId(userId).get();

				System.out.println("Refresh Token 비교!");
				System.out.println(refreshToken);
				System.out.println(refreshTokenService.finduserId(userId).get().getToken());

				if(refreshTokenService.finduserId(userId).get().getToken().equals(refreshToken)) {

					// Refresh Token의 사용자 정보를 기반으로 새로운 Access Token 발급
					String newAccessToken = JWT.create()
							.withSubject(user.getUserId())
							.withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
							.withClaim("id", user.getId())
							.withClaim("userId", user.getUserId())
							.sign(algorithm);

					// 새로 발급한 Access Token을 Response Header에 추가
					response.setHeader(JwtProperties.HEADER_STRING,
							JwtProperties.TOKEN_PREFIX + newAccessToken);
				} else {
					refreshTokenService.delete(userId);
					System.out.println("서버의 Refresh 토큰과 검증한 Refresh 토큰 다름");
					SecurityContextHolder.clearContext();
				}
			} catch (JWTVerificationException exception) {
				// Refresh Token이 유효하지 않은 경우 처리
				SecurityContextHolder.clearContext();
			}
			chain.doFilter(request, response);
			return;
		}

		// 토큰 검증 (이게 인증이기 때문에 AuthenticationManager도 필요 없음)
		// 내가 SecurityContext에 집적접근해서 세션을 만들때 자동으로 UserDetailsService에 있는 loadByUsername이 호출됨.
		try {
			String userId = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET.getBytes())).build()
					.verify(token)
					.getClaim("userId").asString();

			System.out.println("username : " + userId);
			if (userId != null) {
				User user = userRepository.findByUserId(userId).get();
				System.out.println("MyUser : " + user);
				// 인증은 토큰 검증시 끝. 인증을 하기 위해서가 아닌 스프링 시큐리티가 수행해주는 권한 처리를 위해
				// 아래와 같이 토큰을 만들어서 Authentication 객체를 강제로 만들고 그걸 세션에 저장!
				MyUserDetails principalDetails = new MyUserDetails(user);
				System.out.println(principalDetails.getAuthorities());
				Authentication authentication =
						new UsernamePasswordAuthenticationToken(
								principalDetails, //나중에 컨트롤러에서 DI해서 쓸 때 사용하기 편함.
								null, // 패스워드는 모르니까 null 처리, 어차피 지금 인증하는게 아니니까!!
								principalDetails.getAuthorities());

				// 강제로 시큐리티의 세션에 접근하여 값 저장
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

			chain.doFilter(request, response);
		} catch (TokenExpiredException tokenExpiredException) {
			System.out.println("토큰 만료");
			System.out.println(tokenExpiredException);
			response.setStatus(401);
		} catch (Exception e) {
			System.out.println("JWT Exception 오류");
			System.out.println(e);
		}
	}
	
}
