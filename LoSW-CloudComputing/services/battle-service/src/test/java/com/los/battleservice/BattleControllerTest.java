package com.los.battleservice;

import com.los.battleservice.api.BattleController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BattleController.class)
public class BattleControllerTest {

  @Autowired
  MockMvc mvc;

  @Test
  void healthOk() throws Exception {
    mvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"));
  }

  @Test
  void simulateBattleReturnsWinner() throws Exception {
    String body = """
      {
        "playerParty": [{"name":"Arthur","level":1,"attack":10,"defense":5,"maxHp":50,"maxMana":10}],
        "enemyParty": [{"name":"Goblin","level":1,"attack":1,"defense":1,"maxHp":10,"maxMana":0}]
      }
    """;

    mvc.perform(post("/battle/simulate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.winner").exists());
  }
}
