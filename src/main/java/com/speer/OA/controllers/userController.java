package com.speer.OA.controllers;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.speer.OA.models.User;
import com.speer.OA.repositories.UserRepository;
import com.speer.OA.security.JWTService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;


@Controller
@RequestMapping("/auth")
public class userController {
	
	private final UserRepository userRepository;
	
	@Autowired
    public userController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

	@PostMapping(value = {"/signup"})
	public ResponseEntity<String> signUpUser(@RequestBody User user) {
        try {
            userRepository.save(user);
            return ResponseEntity.ok("User signed up");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to sign up user");
        }
	}
	
	@PostMapping(value = {"/login"})
    public ResponseEntity<String> loginUser(@RequestBody User user) {
        // Find the user by username in the database
		User existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found. Please check your username or sign up.");
        }

        // Verify the password
        try {
            if (user.getPassword().equals(existingUser.getPassword())) {
                // Authentication successful, generate and return the access token
                String accessToken = JWTService.generateAccessToken(existingUser.getUsername());
                return ResponseEntity.ok(accessToken);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid credentials. Please check your username and password.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to validate credentials. Please try again later.");
        }
    }
}
