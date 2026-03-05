package com.los.pveservice;

import com.los.pveservice.api.PveController;
import com.los.pveservice.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PveController.class)
public class PveControllerTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  CampaignService campaigns;

  @Test
  void healthOk() throws Exception {
    mvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"));
  }

  @Test
  void startRequiresFields() throws Exception {
    mvc.perform(post("/campaign/start")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{"partyName":"P"}"))
        .andExpect(status().isBadRequest());
  }
}
