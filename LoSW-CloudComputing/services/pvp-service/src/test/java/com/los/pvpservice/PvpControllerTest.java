package com.los.pvpservice;

import com.los.pvpservice.api.PvpController;
import com.los.pvpservice.service.InviteService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PvpController.class)
public class PvpControllerTest {
  @Autowired MockMvc mvc;

  @MockBean org.springframework.web.client.RestTemplate http;
  @MockBean InviteService invites;

  @Test
  void healthOk() throws Exception {
    mvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"));
  }
}
