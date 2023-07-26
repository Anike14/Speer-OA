package com.speer.OA.security;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {
	
	@Value("${JWT.key}")
	private String jwtKey;

	public String extractUserName(String jwToken) {
		return extractClaim(jwToken, Claims::getSubject);
	}
    
    private Date extractExpiration(String jwToken) {
        return extractClaim(jwToken, Claims::getExpiration);
    }
	
	private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = 
    			Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
    	        .getBody();
        return claimsResolvers.apply(claims);
    }

	public boolean isTokenValid(String jwToken, UserDetails userDetails) {
        final String userName = extractUserName(jwToken);
        return (userName.equals(userDetails.getUsername())) && extractExpiration(jwToken).after(new Date());
	}
	
	public String generateAccessToken(String username) {
	    String res = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
	    return res;
	}
	
	@Bean
	public Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
