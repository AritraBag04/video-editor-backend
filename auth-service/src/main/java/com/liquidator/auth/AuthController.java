package com.liquidator.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.regex.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    String jwtSecret = System.getenv("JWT_SECRET");
    PasswordEncoder passwordEncoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    @Autowired
    private UserRepository userRepository;
    // This class will handle authentication-related endpoints
    // For example, login, logout, and user signup can be implemented here

     // Example endpoint for user login
     @PostMapping("/login")
     public ResponseEntity<?> login(@RequestBody User user) {
         // Logic for user authentication
         if(user.getEmail() == null || user.getPassword() == null) {
             return ResponseEntity.badRequest().body("Email and password must not be null");
         }
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser == null || !passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

         byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
         SecretKey key = Keys.hmacShaKeyFor(keyBytes);

         String token = Jwts.builder()
                 .setSubject(existingUser.getUserId().toString())
                 .setIssuedAt(new Date())
                 .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                 .signWith(SignatureAlgorithm.HS256, key)
                 .compact();

         return ResponseEntity.ok(Map.of("token", token));
     }

     // Example endpoint for user signup
     @PostMapping("/signup")
     public ResponseEntity<?> signup(@RequestBody User user) {
         log.info("Received signup request for user: {}", user.getEmail());
         try {
             if (!isValidEmail(user.getEmail())) {
                 return ResponseEntity.badRequest().body("Invalid email format");
             }
         } catch(Exception e){
             log.error("The received email: {}", user.getEmail());
         }
        User secureUser = User.builder()
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .build();
        userRepository.save(secureUser);
        return ResponseEntity.ok("User signed up successfully");
     }
}
