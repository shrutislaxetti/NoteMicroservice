package com.bridgelabz.notemicroservice.util;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JWTokenProvider {
	
	//@Value("${key}")
	private String key="shruti";

	public String tokenGenerator(String userId) {

		long nowMillis = System.currentTimeMillis() + (20 * 60 * 60 * 1000);
		Date now = new Date(nowMillis);

		JwtBuilder builder = Jwts.builder().setIssuedAt(now).setSubject(userId)
				.signWith(SignatureAlgorithm.HS256, key);

		return builder.compact();
	}

	public String parseJWT(String jwt) {

		
		Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(key)).parseClaimsJws(jwt)
				.getBody();
		System.out.println("Subject: " + claims.getSubject());
		return claims.getSubject();

	}

}