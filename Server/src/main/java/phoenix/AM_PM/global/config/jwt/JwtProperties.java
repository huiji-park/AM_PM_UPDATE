package phoenix.AM_PM.global.config.jwt;

public interface JwtProperties {
	String SECRET = "PhoenixkOsAsTuDyPrOjEcT2uNiMeEtInGjWtByAeLiMkOsAsTuDyPrOjEcT2uNiMeEtInGjWtByAeLiMkOsAsTuDyPrOjEcT2uNiMeEtInGjWtByAeLiMkOsAsTuDyPrOjEcT2uNiMeEtInGjWtByAeLiMkOsAsTuDyPrOjEcT2uNiMeEtInGjWtByAeLiM"; // 우리 서버만 알고 있는 비밀값

	int EXPIRATION_TIME = 30 * 1000 * 60; // 30분
	String TOKEN_PREFIX = "Bearer ";
	String HEADER_STRING = "Authorization";

	String REFRESH_TOKEN_PREFIX = "Bearer ";
	String REFRESH_TOKEN_HEADER = "RefreshToken";
	long REFRESH_TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24시간 (예시)
}
