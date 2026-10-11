package phoenix.AM_PM.global.config.service;

import io.jsonwebtoken.*;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import phoenix.AM_PM.global.config.jwt.JwtProperties;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final String secretKey = JwtProperties.SECRET;

    @Override
    public String getToken(String key, Object value, int addTime) {
        Date d = new Date();
//        log.info(d.toString() +" : " + d.getTime());
        d.setTime(d.getTime()+(addTime));  // 테스트를 위해서 3분간만 유효한 토큰을 만듬
//        log.info(d.toString() +" : " + d.getTime());

        byte[] secretByteKey = DatatypeConverter.parseBase64Binary(secretKey);
        Key signKey = new SecretKeySpec(secretByteKey, SignatureAlgorithm.HS512.getJcaName());

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("typ", "JWT");
        headerMap.put("alg", "HS512");

        Map<String, Object> map = new HashMap<>();
        map.put(key, value);

        JwtBuilder builder = Jwts.builder().setHeader(headerMap)
                .setClaims(map)
                .setExpiration(d)
                .signWith(signKey, SignatureAlgorithm.HS512);
        return "Bearer "+builder.compact();
    }

    @Override
    public Claims getClaims(String Token) {
        log.info("getClaims() 호출 : "+Token);
        if (Token != null && !"".equals(Token)) {
            String token = Token.replace(JwtProperties.TOKEN_PREFIX, "");
            try {
//                byte[] secretByteKey = DatatypeConverter.parseBase64Binary(secretKey);
                Key signKey = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName());
                return Jwts.parserBuilder().setSigningKey(signKey).build().parseClaimsJws(token).getBody();
            } catch (ExpiredJwtException e) {
                log.error("토큰 만료");
                System.out.println("토큰 만료!!");
            } catch (JwtException e) {
                log.error("토큰 유효하지 않음");
                System.out.println("유효 토큰 x");
                System.out.println(e);
            }
        }
        return null;
    }

    @Override
    public boolean isValid(String token) {
        return this.getClaims(token) != null;
    }

    @Override
    public String getId(String token) {
        Claims claims = this.getClaims(token);
        if(claims != null){
            return claims.get("userId").toString();
        }
        return "";
    }
}