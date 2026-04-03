package com.legends.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemIntegrationTest {

    private static long   userId;
    private static String username = "int_test_user_" + System.currentTimeMillis();
    private static String password = "testpassword";

    @Container
    static DockerComposeContainer<?> stack = new DockerComposeContainer<>(
            new File("../docker-compose.yml"))
        .withExposedService("gateway",         8080,
                Wait.forHttp("/health").forPort(8080).withStartupTimeout(Duration.ofSeconds(120)))
        .withExposedService("profile-service", 5000,
                Wait.forHttp("/api/profile/health").forPort(5000).withStartupTimeout(Duration.ofSeconds(90)))
        .withExposedService("battle-service",  5001,
                Wait.forHttp("/api/battle/health").forPort(5001).withStartupTimeout(Duration.ofSeconds(60)))
        .withExposedService("pve-service",     5002,
                Wait.forHttp("/api/pve/health").forPort(5002).withStartupTimeout(Duration.ofSeconds(90)))
        .withExposedService("data-service",    5003,
                Wait.forHttp("/api/data/health").forPort(5003).withStartupTimeout(Duration.ofSeconds(90)))
        .withExposedService("pvp-service",     5004,
                Wait.forHttp("/api/pvp/health").forPort(5004).withStartupTimeout(Duration.ofSeconds(60)))
        .withLocalCompose(true);

    @BeforeAll
    static void configureRestAssured() {
        String host = stack.getServiceHost("gateway", 8080);
        int    port = stack.getServicePort("gateway", 8080);
        RestAssured.baseURI = "http://" + host;
        RestAssured.port    = port;
    }

    // IT-TC-01
    @Test @Order(1)
    void registerNewUser_shouldSucceed() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
        .when()
            .post("/api/profile/register")
        .then()
            .statusCode(200)
            .body("success",  is(true))
            .body("username", equalTo(username));
    }

    // IT-TC-02
    @Test @Order(2)
    void registerDuplicate_shouldFail() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\",\"password\":\"otherpass\"}")
        .when()
            .post("/api/profile/register")
        .then()
            .statusCode(400)
            .body("success", is(false));
    }

    // IT-TC-03
    @Test @Order(3)
    void login_shouldReturnProfileData() {
        userId = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
        .when()
            .post("/api/profile/login")
        .then()
            .statusCode(200)
            .body("success",          is(true))
            .body("username",         equalTo(username))
            .body("userId",           notNullValue())
            .body("scores",           equalTo(0))
            .body("campaignProgress", equalTo(0))
            .extract().path("userId");
    }

    // IT-TC-04
    @Test @Order(4)
    void startPveCampaign_shouldReturnRoom0() {
        given()
            .contentType(ContentType.JSON)
            .body("[{\"name\":\"TEST\",\"heroClass\":\"WARRIOR\"," +
                  "\"level\":1,\"attack\":5,\"defense\":5," +
                  "\"hp\":100,\"maxHp\":100,\"mana\":50,\"maxMana\":50}]")
        .when()
            .post("/api/pve/" + userId + "/start")
        .then()
            .statusCode(200)
            .body("success",     is(true))
            .body("currentRoom", equalTo(0));
    }

    // IT-TC-05
    @Test @Order(5)
    void nextRoom_shouldAdvanceRoomCounter() {
        given()
        .when()
            .post("/api/pve/" + userId + "/next-room")
        .then()
            .statusCode(200)
            .body("success",     is(true))
            .body("currentRoom", equalTo(1))
            .body("roomType",    either(equalTo("BATTLE")).or(equalTo("INN")));
    }

    // IT-TC-06
    @Test @Order(6)
    void battleService_healthEndpointResponds() {
        String battleHost = stack.getServiceHost("battle-service", 5001);
        int    battlePort = stack.getServicePort("battle-service", 5001);
        given()
            .baseUri("http://" + battleHost)
            .port(battlePort)
        .when()
            .get("/api/battle/health")
        .then()
            .statusCode(200);
    }

    // IT-TC-07
    @Test @Order(7)
    void saveCampaign_shouldPersistState() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\":" + userId + "," +
                  "\"partyName\":\"IntTestParty\"," +
                  "\"currentRoom\":1," +
                  "\"gold\":500," +
                  "\"heroes\":[{\"name\":\"Aldric\",\"heroClass\":\"WARRIOR\"," +
                  "\"level\":1,\"attack\":5,\"defense\":5," +
                  "\"hp\":100,\"maxHp\":100,\"mana\":50,\"maxMana\":50,\"experience\":0}]}")
        .when()
            .post("/api/data/campaign/save")
        .then()
            .statusCode(200)
            .body("partyName",   equalTo("IntTestParty"))
            .body("currentRoom", equalTo(1));
    }

    // IT-TC-08
    @Test @Order(8)
    void loadSavedCampaign_shouldRestoreRoom() {
        given()
        .when()
            .get("/api/data/campaign/" + userId)
        .then()
            .statusCode(200)
            .body("partyName",   equalTo("IntTestParty"))
            .body("currentRoom", equalTo(1))
            .body("gold",        equalTo(500));
    }

    // IT-TC-09
    @Test @Order(9)
    void saveScore_thenRetrieveBestScore() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"score\":1500}")
        .when()
            .post("/api/data/scores/" + userId)
        .then()
            .statusCode(200);

        given()
        .when()
            .get("/api/data/scores/" + userId + "/best")
        .then()
            .statusCode(200)
            .body("bestScore", equalTo(1500));
    }

    // IT-TC-10
    @Test @Order(10)
    void leaderboard_shouldReturnNonEmptyList() {
        given()
        .when()
            .get("/api/data/scores/top?limit=5")
        .then()
            .statusCode(200)
            .body("$", not(empty()));
    }

    // IT-TC-11
    @Test @Order(11)
    void pvpInvite_toNonExistentPlayer_shouldReturn400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"fromUsername\":\"" + username + "\",\"toUsername\":\"INVITESUBJECT\"}")
        .when()
            .post("/api/pvp/invite")
        .then()
            .statusCode(400)
            .body("error", containsString("not found"));
    }

    // IT-TC-12
    @Test @Order(12)
    void gateway_routesProfileServiceCorrectly() {
        given()
        .when()
            .get("/api/profile/" + userId)
        .then()
            .statusCode(200)
            .body("username", equalTo(username));
    }
}
