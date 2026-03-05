package com.los.pveservice.service;

import com.los.pveservice.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CampaignService {
  private final RestTemplate http;
  private final String dataBaseUrl;
  private final String battleBaseUrl;

  private final Map<Integer, CampaignState> stateByUser = new ConcurrentHashMap<>();

  public CampaignService(RestTemplate http,
                         @Value("${services.dataBaseUrl}") String dataBaseUrl,
                         @Value("${services.battleBaseUrl}") String battleBaseUrl) {
    this.http = http;
    this.dataBaseUrl = dataBaseUrl;
    this.battleBaseUrl = battleBaseUrl;
  }

  public CampaignState start(StartCampaignRequest req) {
    CampaignState state = new CampaignState(req.userId(), req.partyName(), 1, 0, req.heroes());
    stateByUser.put(req.userId(), state);
    save(req.userId());
    return state;
  }

  public NextRoomResponse nextRoom(int userId) {
    CampaignState current = stateByUser.get(userId);
    if (current == null) {
      // try load from DB
      CampaignState loaded = load(userId);
      current = loaded;
    }
    int nextRoom = current.currentRoom() + 1;
    String roomType = (nextRoom % 2 == 0) ? "BATTLE" : "INN";

    String battleWinner = null;
    int gold = current.gold();

    if ("BATTLE".equals(roomType)) {
      // simple enemy party
      Map<String,Object> battleReq = Map.of(
          "playerParty", current.heroes().stream().map(h -> Map.of(
              "name", h.heroName(),
              "level", h.heroLevel(),
              "attack", h.attack(),
              "defense", h.defense(),
              "maxHp", h.maxHp(),
              "maxMana", 10
          )).toList(),
          "enemyParty", List.of(Map.of(
              "name","Goblin",
              "level", Math.max(1, nextRoom/2),
              "attack", 3 + nextRoom,
              "defense", 2 + nextRoom/2,
              "maxHp", 20 + nextRoom*5,
              "maxMana", 0
          ))
      );
      Map res = http.postForObject(battleBaseUrl + "/battle/simulate", battleReq, Map.class);
      battleWinner = (String) res.get("winner");
      if ("PLAYER".equals(battleWinner)) {
        gold += 10;
      }
    } else {
      gold += 2; // inn bonus
    }

    CampaignState updated = new CampaignState(userId, current.partyName(), nextRoom, gold, current.heroes());
    stateByUser.put(userId, updated);
    save(userId);

    return new NextRoomResponse(nextRoom, roomType, battleWinner, gold);
  }

  public Map<?,?> save(int userId) {
    CampaignState state = stateByUser.get(userId);
    if (state == null) throw new IllegalStateException("No campaign loaded");
    Map<String,Object> payload = new HashMap<>();
    payload.put("userId", state.userId());
    payload.put("partyName", state.partyName());
    payload.put("currentRoom", state.currentRoom());
    payload.put("gold", state.gold());
    payload.put("heroes", state.heroes().stream().map(h -> Map.of(
        "heroId", null,
        "heroName", h.heroName(),
        "heroLevel", h.heroLevel(),
        "maxHp", h.maxHp(),
        "attack", h.attack(),
        "defense", h.defense()
    )).toList());

    ResponseEntity<Map> res = http.postForEntity(dataBaseUrl + "/campaign/save", payload, Map.class);
    return res.getBody();
  }

  public CampaignState load(int userId) {
    Map loaded = http.getForObject(dataBaseUrl + "/campaign/load/" + userId, Map.class);
    // loaded is CampaignStateDto-like: {partyId,userId,partyName,currentRoom,gold,heroes:[...]}
    List<Map<String,Object>> heroesRaw = (List<Map<String,Object>>) loaded.get("heroes");
    List<HeroDto> heroes = heroesRaw.stream().map(h -> new HeroDto(
        (String) h.get("heroName"),
        ((Number) h.get("heroLevel")).intValue(),
        ((Number) h.get("maxHp")).intValue(),
        ((Number) h.get("attack")).intValue(),
        ((Number) h.get("defense")).intValue()
    )).toList();
    CampaignState state = new CampaignState(
        ((Number) loaded.get("userId")).intValue(),
        (String) loaded.get("partyName"),
        ((Number) loaded.get("currentRoom")).intValue(),
        ((Number) loaded.get("gold")).intValue(),
        heroes
    );
    stateByUser.put(userId, state);
    return state;
  }
}
