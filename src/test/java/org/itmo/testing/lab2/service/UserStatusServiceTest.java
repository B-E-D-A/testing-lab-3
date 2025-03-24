package org.itmo.testing.lab2.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStatusServiceTest {

    private UserAnalyticsService userAnalyticsService;
    private UserStatusService userStatusService;

    @BeforeAll
    void setUp() {
        userAnalyticsService = mock(UserAnalyticsService.class);
        userStatusService = new UserStatusService(userAnalyticsService);
    }

    @ParameterizedTest
    @CsvSource({
            "user0, 90, Active",
            "user1, 60, Active",
            "user2, 0, Inactive",
            "user3, 10, Inactive",
            "user4, 121, Highly active"
    })
    @DisplayName("getUserStatus")
    void getUserStatus_ReturnsCorrectStatus(String userId, long totalActivityTime, String expectedStatus) {
        when(userAnalyticsService.getTotalActivityTime(userId)).thenReturn(totalActivityTime);
        assertAll(
                () -> assertEquals(expectedStatus, userStatusService.getUserStatus(userId)),
                () -> verify(userAnalyticsService).getTotalActivityTime(userId)
        );
    }

    @Nested
    @DisplayName("getUserLastSessionDate")
    class GetUserLastSessionDateTests {
        @Test
        @DisplayName("basic scenario")
        public void testGetUserLastSessionDate() {
            UserAnalyticsService.Session session1 = mock(UserAnalyticsService.Session.class);
            UserAnalyticsService.Session session2 = mock(UserAnalyticsService.Session.class);

            when(session1.getLogoutTime()).thenReturn(LocalDateTime.of(2025, 1, 17, 19, 0));
            when(session2.getLogoutTime()).thenReturn(LocalDateTime.of(2025, 3, 10, 2, 1));

            when(userAnalyticsService.getUserSessions("user1")).thenReturn(List.of(session1, session2));

            Optional<String> lastSessionDate = userStatusService.getUserLastSessionDate("user1");

            assertAll(
                    () -> assertTrue(lastSessionDate.isPresent()),
                    () -> assertEquals("2025-03-10", lastSessionDate.get()),
                    () -> verify(userAnalyticsService).getUserSessions("user1")
            );
        }

        @Test
        @DisplayName("empty result")
        void getUserLastSessionDate_ReturnsEmpty() { //не обрабатывает случай пустого списка
            when(userAnalyticsService.getUserSessions("user2")).thenReturn(List.of());
            Optional<String> lastSessionDate = userStatusService.getUserLastSessionDate("user2");
            assertAll(
                    () -> assertTrue(lastSessionDate.isEmpty()),
                    () -> verify(userAnalyticsService).getUserSessions("user2")
            );
        }
    }
}
