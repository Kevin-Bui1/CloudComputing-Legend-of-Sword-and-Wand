package com.los.dataservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(com.los.dataservice.repo.DataRepository.class)
@Sql(scripts = "/schema.sql")
public class DataRepositoryTest {

    @Autowired
    com.los.dataservice.repo.DataRepository repo;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void createAndFindUser() {
        int id = repo.createUser(new com.los.dataservice.dto.CreateUserRequest("u1","p1"));
        assertTrue(id > 0);
        var u = repo.findUserByUsername("u1");
        assertTrue(u.isPresent());
        assertEquals("u1", u.get().username());
        assertTrue(repo.verifyLogin("u1","p1").isPresent());
        assertFalse(repo.verifyLogin("u1","wrong").isPresent());
    }

    @Test
    void saveAndLoadCampaign() {
        int userId = repo.createUser(new com.los.dataservice.dto.CreateUserRequest("u2","p2"));
        var heroes = java.util.List.of(new com.los.dataservice.dto.HeroDto(null,"Arthur",1,100,10,5));
        int partyId = repo.saveCampaign(new com.los.dataservice.dto.SaveCampaignRequest(userId,"Party",1,0,heroes));
        assertTrue(partyId > 0);
        var state = repo.loadActiveCampaign(userId);
        assertTrue(state.isPresent());
        assertEquals("Party", state.get().partyName());
        assertEquals(1, state.get().heroes().size());
    }
}
