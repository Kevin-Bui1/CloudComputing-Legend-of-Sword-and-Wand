package com.los.profileservice;

import com.los.profileservice.api.ProfileController;
import com.los.profileservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
public class ProfileControllerTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  AuthService auth;

  @Test
  void healthOk() throws Exception {
    mvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"));
  }

  @Test
  void registerReturnsSuccess() throws Exception {
    Mockito.when(auth.register(Mockito.any())).thenReturn(Map.of("status","success","userId",1));
    mvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{"username":"u","password":"p"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"));
  }
}
