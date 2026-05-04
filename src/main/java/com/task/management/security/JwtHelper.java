package com.task.management.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.task.management.io.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
@Component
public class JwtHelper {

    private static final Logger logger = LoggerFactory.getLogger(JwtHelper.class);

    // requirement :
    public static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60;// 24 hours
    

//     public static final long JWT_TOKEN_VALIDITY = 60;
    @Value("${jwt.secret}")
    private String secret;

    // retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getSubject);
        } catch (Exception e) {
            logger.error("Error getting username from token", e);
            return null;
        }
    }

    // retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getExpiration);
        } catch (Exception e) {
            logger.error("Error getting expiration date from token", e);
            return null;
        }
    }
    // retrieve expiration date from jwt token
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            logger.error("Error getting claim from token", e);
            return null;
        }
    }
     

    // for retrieving any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            logger.error("Error parsing JWT token", e);
            throw e;
        }
    }

    // check if the token has expired
    private Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            logger.warn("Token has no expiration date, treating as expired");
            return true;
        }

        boolean expired = expiration.before(new Date());
        if (expired) {
            logger.warn("JWT token expired at {}. Current time: {}", expiration, new Date());
        }
        return expired;
    }


    // generate token for user
    public String generateToken(UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();

        // 🔹 Add role
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("USER");

        claims.put("role", role);

        // 🔥 Add tenantId (only if using CustomUserDetails)
        if (userDetails instanceof CustomUserDetails customUser) {
            claims.put("tenantId", customUser.getTenantId());
        }

        return doGenerateToken(claims, userDetails.getUsername());
    }

    // while creating the token -
    // 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
    // 2. Sign the JWT using the HS512 algorithm and secret key.
    // 3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    // compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        String token = Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret).compact();

        logger.info("Generated JWT token for subject: {}", subject);
        return token;
    }

    // validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        boolean isValid = (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        logger.info("Token validation result for user {} : {}", username, isValid);
        return isValid;
    }
}