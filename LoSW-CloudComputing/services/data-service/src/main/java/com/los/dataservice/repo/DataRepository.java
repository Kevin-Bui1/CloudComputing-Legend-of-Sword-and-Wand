package com.los.dataservice.repo;

import com.los.dataservice.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DataRepository {
    private final JdbcTemplate jdbc;

    public DataRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int createUser(CreateUserRequest req) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Users(username, password) VALUES(?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, req.username());
            ps.setString(2, req.password());
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key == null ? -1 : key.intValue();
    }

    public Optional<UserDto> findUserByUsername(String username) {
        List<UserDto> rows = jdbc.query(
                "SELECT user_id, username, pvp_wins, pvp_losses FROM Users WHERE username = ?",
                (rs, i) -> new UserDto(rs.getInt("user_id"), rs.getString("username"),
                        rs.getInt("pvp_wins"), rs.getInt("pvp_losses")),
                username
        );
        return rows.stream().findFirst();
    }

    public Optional<UserDto> findUserById(int userId) {
        List<UserDto> rows = jdbc.query(
                "SELECT user_id, username, pvp_wins, pvp_losses FROM Users WHERE user_id = ?",
                (rs, i) -> new UserDto(rs.getInt("user_id"), rs.getString("username"),
                        rs.getInt("pvp_wins"), rs.getInt("pvp_losses")),
                userId
        );
        return rows.stream().findFirst();
    }

    public boolean incrementPvpWinLoss(int winnerId, int loserId) {
        int a = jdbc.update("UPDATE Users SET pvp_wins = pvp_wins + 1 WHERE user_id = ?", winnerId);
        int b = jdbc.update("UPDATE Users SET pvp_losses = pvp_losses + 1 WHERE user_id = ?", loserId);
        return a == 1 && b == 1;
    }

    
    public Optional<UserDto> verifyLogin(String username, String password) {
        List<UserDto> rows = jdbc.query(
                "SELECT user_id, username, pvp_wins, pvp_losses FROM Users WHERE username = ? AND password = ?",
                (rs, i) -> new UserDto(rs.getInt("user_id"), rs.getString("username"),
                        rs.getInt("pvp_wins"), rs.getInt("pvp_losses")),
                username, password
        );
        return rows.stream().findFirst();
    }

    /** Save campaign into Parties + Heroes (simple overwrite: deactivate old active campaign, create new party). */
    public int saveCampaign(SaveCampaignRequest req) {
        // mark existing active campaign parties inactive
        jdbc.update("UPDATE Parties SET is_active_campaign = FALSE WHERE user_id = ?", req.userId());

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Parties(user_id, party_name, current_room, gold, is_active_campaign) VALUES(?, ?, ?, ?, TRUE)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, req.userId());
            ps.setString(2, req.partyName());
            ps.setInt(3, req.currentRoom());
            ps.setInt(4, req.gold());
            return ps;
        }, kh);
        int partyId = kh.getKey().intValue();

        if (req.heroes() != null) {
            for (HeroDto h : req.heroes()) {
                jdbc.update("INSERT INTO Heroes(party_id, hero_name, hero_level, max_hp, attack, defense) VALUES(?, ?, ?, ?, ?, ?)",
                        partyId, h.heroName(), h.heroLevel(), h.maxHp(), h.attack(), h.defense());
            }
        }
        return partyId;
    }

    public Optional<CampaignStateDto> loadActiveCampaign(int userId) {
        List<Map<String,Object>> parties = jdbc.queryForList(
                "SELECT party_id, user_id, party_name, current_room, gold FROM Parties WHERE user_id = ? AND is_active_campaign = TRUE ORDER BY party_id DESC LIMIT 1",
                userId
        );
        if (parties.isEmpty()) return Optional.empty();
        Map<String,Object> p = parties.get(0);
        int partyId = ((Number)p.get("party_id")).intValue();
        List<HeroDto> heroes = jdbc.query(
                "SELECT hero_id, hero_name, hero_level, max_hp, attack, defense FROM Heroes WHERE party_id = ?",
                (rs, i) -> new HeroDto(rs.getInt("hero_id"), rs.getString("hero_name"),
                        rs.getInt("hero_level"), rs.getInt("max_hp"),
                        rs.getInt("attack"), rs.getInt("defense")),
                partyId
        );
        return Optional.of(new CampaignStateDto(
                partyId,
                ((Number)p.get("user_id")).intValue(),
                (String)p.get("party_name"),
                ((Number)p.get("current_room")).intValue(),
                ((Number)p.get("gold")).intValue(),
                heroes
        ));
    }
}
