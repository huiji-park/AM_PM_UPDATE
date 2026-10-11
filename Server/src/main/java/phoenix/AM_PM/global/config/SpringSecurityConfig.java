package phoenix.AM_PM.global.config;

		import lombok.RequiredArgsConstructor;
		import org.springframework.beans.factory.annotation.Autowired;
		import org.springframework.context.annotation.Bean;
		import org.springframework.context.annotation.Configuration;
		import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
		import org.springframework.security.authentication.AuthenticationManager;
		import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
		import org.springframework.security.config.annotation.web.builders.HttpSecurity;
		import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
		import org.springframework.security.config.http.SessionCreationPolicy;
		import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
		import org.springframework.security.web.SecurityFilterChain;
		import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
		import phoenix.AM_PM.domain.refrash.service.RefreshTokenService;
		import phoenix.AM_PM.domain.user.repository.UserRepository;
		import phoenix.AM_PM.domain.user.service.UserService;
		import phoenix.AM_PM.global.config.jwt.JwtAuthenticationFilter;
		import phoenix.AM_PM.global.config.jwt.JwtAuthorizationFilter;
		import phoenix.AM_PM.global.config.oauth.OAuth2SuccessHandler;
		import phoenix.AM_PM.global.config.oauth.Oauth2UserCustomService;

@Configuration
@EnableWebSecurity // 시큐리티 활성화 -> 기본 스프링 필터체인에 등록
public class SpringSecurityConfig {

	@Autowired
	private UserRepository myUserRepository;

	@Autowired
	private CorsConfig corsConfig;

	@Autowired
	private AuthenticationConfiguration authenticationConfiguration;

	@Autowired
	private Oauth2UserCustomService oauth2UserCustomService;

	@Autowired
	private UserService userService;
	@Autowired
	private RefreshTokenService refreshTokenService;

	@Bean
	public OAuth2SuccessHandler oAuth2SuccessHandler() {
		return new OAuth2SuccessHandler(userService, refreshTokenService);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.addFilter(corsConfig.corsFilter())
				.csrf((csrfConfig) ->
						csrfConfig.disable()
				)
				.sessionManagement((sessionManagement) ->
						sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin((formLogin) -> formLogin.disable())
				.httpBasic((httpBasic) -> httpBasic.disable())
				.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
				.addFilter(jwtAuthorizationFilter())
				.authorizeRequests(authorizeRequests ->
						authorizeRequests
								.requestMatchers("/**").permitAll()
//								.requestMatchers("/api/auth/local").permitAll()
//								.requestMatchers("/api/user").permitAll()
//								.requestMatchers("/api/user/user_id/**").permitAll()
//								.requestMatchers("/api/user/email/**").permitAll()
								.anyRequest().authenticated())
				.oauth2Login(oauth2Login ->
						oauth2Login
								.successHandler(oAuth2SuccessHandler())
								.userInfoEndpoint(userInfoEndpoint ->
										userInfoEndpoint.userService(oauth2UserCustomService)
								)
				);

		return http.build();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
		System.out.println("등록");
		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManagerBean());
		return jwtAuthenticationFilter;
	}

	@Bean
	public JwtAuthorizationFilter jwtAuthorizationFilter() throws Exception {
		return new JwtAuthorizationFilter(authenticationManagerBean(), myUserRepository);
	}
}
