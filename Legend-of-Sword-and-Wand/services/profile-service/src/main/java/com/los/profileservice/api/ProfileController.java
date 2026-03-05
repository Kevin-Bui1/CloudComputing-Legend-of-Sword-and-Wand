package com.los.profileservice.api;

import com.los.profileservice.dto.LoginRequest;
import com.los.profileservice.dto.RegisterRequest;
import com.los.profileservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@RestController
public class ProfileController {
  private final AuthService auth;

  public ProfileController(AuthService auth) { this.auth = auth; }

  @GetMapping("/health")
  public Map<String,String> health() { return Map.of("status","ok"); }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
    try {
      return ResponseEntity.ok(auth.register(req));
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    try {
      return ResponseEntity.ok(auth.login(req));
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
    }
  }

  @GetMapping("/profile/{userId}")
  public ResponseEntity<?> profile(@PathVariable int userId) {
    try {
      return ResponseEntity.ok(auth.getProfile(userId));
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
    }
  }
}
