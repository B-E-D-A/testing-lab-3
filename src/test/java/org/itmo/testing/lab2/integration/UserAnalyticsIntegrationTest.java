package org.itmo.testing.lab2.integration;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.itmo.testing.lab2.controller.UserAnalyticsController;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAnalyticsIntegrationTest {

    private Javalin app;
    private int port = 7000;

    @BeforeAll
    void setUp() {
        app = UserAnalyticsController.createApp();
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    void tearDown() {
        app.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Register user - successful")
    void testUserRegistration() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    @Test
    @Order(2)
    @DisplayName("Register user - error - user is already registered")
    void testUserRegistration_AlreadyRegistered() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Fake Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("User already exists"));
    }

    @Test
    @Order(3)
    @DisplayName("Register user - error - without params")
    void testUserRegistrationMissingParams() {
        given()
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
        given()
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(4)
    @DisplayName("Record Session - successful")
    void testRecordSession() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200)
                .body(equalTo("Session recorded"));
    }

    @Test
    @Order(5)
    @DisplayName("Record Session - error - without params")
    void testRecordSessionMissingParams() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
        given()
                .queryParam("userId", "user1")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(6)
    @DisplayName("Record Session - error - Invalid format")
    void testRecordSessionInvalidTimeFormat() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", "NoIdeaHowToWriteTime")
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }

    @Test
    @Order(7)
    @DisplayName("Record Session - error - logout < login Time")
    void testRecordSessionLogoutTimeBeforeLoginTime() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.toString())
                .queryParam("logoutTime", now.minusHours(1).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }

    @Test
    @Order(8)
    @DisplayName("totalActivity - successful")
    void testGetTotalActivity() {
        given()
                .queryParam("userId", "user1")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(200)
                .body(containsString("Total activity:"))
                .body(containsString("minutes"));
    }

    @Test
    @Order(9)
    @DisplayName("totalActivity - error - no userId")
    void testGetTotalActivityMissingUserId() {
        given()
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing userId"));
    }

    @Test
    @Order(10)
    @DisplayName("inactiveUsers - successful")
        //определил, что не настроен Object Mapper для List в build.gradle
    void testGetInactiveUsers() {
        given()
                .queryParam("days", "1")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(11)
    @DisplayName("inactiveUsers - error - without params")
    void testGetInactiveUsersMissingDays() {
        given()
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Missing days parameter"));
    }

    @Test
    @Order(12)
    @DisplayName("inactiveUsers - error - Invalid format")
    void testGetInactiveUsersInvalidDays() {
        given()
                .queryParam("days", "Word")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid number format for days"));
        given()
                .queryParam("days", "-1")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(containsString("Invalid number format for days"));
    }

    @Test
    @Order(13)
    @DisplayName("monthlyActivity - successful")
    void testGetMonthlyActivity() {
        given()
                .queryParam("userId", "user1")
                .queryParam("month", "2025-02")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    @Order(14)
    @DisplayName("monthlyActivity - error - without params")
    void testGetMonthlyActivityMissingParams() {
        given()
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
        given()
                .queryParam("userId", "user1")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(15)
    @DisplayName("monthlyActivity - error - Invalid month param")
    void testGetMonthlyActivityInvalidMonth() {
        given()
                .queryParam("userId", "user1")
                .queryParam("month", "NoIdeaHowToWriteMonth")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }
}
