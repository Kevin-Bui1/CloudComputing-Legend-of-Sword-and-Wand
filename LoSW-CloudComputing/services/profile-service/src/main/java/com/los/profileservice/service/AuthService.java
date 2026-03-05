package com.los.profileservice.service;

import com.los.profileservice.dto.LoginRequest;
import com.los.profileservice.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthService {
  private final RestTemplate http;
  private final String dataBaseUrl;

  public AuthService(RestTemplate http, @Value("${data.baseUrl}") String dataBaseUrl) {
    this.http = http;
    this.dataBaseUrl = dataBaseUrl;
  }

  public Map<?,?> register(RegisterRequest req) {
    ResponseEntity<Map> res = http.postForEntity(dataBaseUrl + "/users", req, Map.class);
    return res.getBody();
  }

  public Map<?,?> login(LoginRequest req) {
    ResponseEntity<Map> res = http.postForEntity(
        dataBaseUrl + "/auth/verify",
        Map.of("username", req.username(), "password", req.password()),
        Map.class
    );
    return res.getBody();
  }

  public Map<?,?> getProfile(int userId) {
    return http.getForObject(dataBaseUrl + "/users/" + userId, Map.class);
  }
}
