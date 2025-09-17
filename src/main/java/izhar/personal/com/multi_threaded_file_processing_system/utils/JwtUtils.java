package izhar.personal.com.multi_threaded_file_processing_system.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Component
public class JwtUtils {


  public static final String key = "izharadfjldfjasldfasdflasdjf;aldfj;asdfjasd;f";

  
  public String generateToken(String userName) {
    HashMap<String, Object> claims = new HashMap<String, Object>();

    return createToken(userName, claims);
  }

  public boolean validateToken(String userName, String token) {
    return userName.equals(extractUserEmail(token)) && isTokenExpired(token);
  }

  public boolean isTokenExpired(String token) {
    return extractAllClaims(token).getExpiration().before(new Date());
  }

  public String extractUserEmail(String token) {
    return extractAllClaims(token).getSubject();
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith((SecretKey) getSigningKey(key)).build().parseSignedClaims(token).getPayload();
  }


  private String createToken(String subject, Object claims) {
    return Jwts.builder().header().keyId("akeyID").add("typ", "JWT").add("alg", "HS256").and().subject(subject).
          issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 24)).signWith(getSigningKey(key)).compact();
  }


  private Key getSigningKey(String key) {
    return Keys.hmacShaKeyFor(key.getBytes());
  }

}
