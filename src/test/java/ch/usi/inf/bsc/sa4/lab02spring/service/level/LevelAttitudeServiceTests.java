package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitude;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitudeType;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttitudeRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = LevelAttitudeService.class)
@DisplayName("The LevelAttitude Service")
@SuppressWarnings("NullAway")
class LevelAttitudeServiceTests {

    private static final String USER_ID = "u1";
    private static final String LEVEL_ID = "l1";

    @MockitoBean
    private LevelRepository levelRepository;

    @MockitoBean
    private AttitudeRepository attitudeRepository;

    @MockitoBean
    private UserService userService;

    @Autowired
    private LevelAttitudeService service;

    private User user;
    private Level level;
    private LevelAttitude expectedNewAttitude;

    @BeforeEach
    void setUp() {
        user = new User(USER_ID, "Name");
        level = new Level("T", "D", user);
        expectedNewAttitude = new LevelAttitude(user, level, LevelAttitudeType.LIKE);
    }

    @Test
    @DisplayName("setAttitude throws when user missing")
    void setAttitudeUserMissing() {
        when(userService.getById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.setAttitude(USER_ID, LEVEL_ID, LevelAttitudeType.LIKE));
    }

    @Test
    @DisplayName("setAttitude throws when level missing")
    void setAttitudeLevelMissing() {
        when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

        assertThrows(LevelNotFoundException.class,
                () -> service.setAttitude(USER_ID, LEVEL_ID, LevelAttitudeType.LIKE));
    }

    @Test
    @DisplayName("setAttitude updates existing attitude")
    void setAttitudeUpdatesExisting() {
        when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

        final LevelAttitude existing = new LevelAttitude(user, level, LevelAttitudeType.DISLIKE);
        when(attitudeRepository.findByLevelAndUser(level, user)).thenReturn(Optional.of(existing));
        when(attitudeRepository.save(existing)).thenAnswer(i -> i.getArgument(0));

        final LevelAttitude result = service.setAttitude(USER_ID, LEVEL_ID, LevelAttitudeType.LIKE);

        assertEquals(LevelAttitudeType.LIKE, result.getAttitude());
        verify(attitudeRepository).save(existing);
    }

    @Test
    @DisplayName("setAttitude creates new attitude when none exists")
    void setAttitudeCreatesNew() {
        when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
        when(attitudeRepository.findByLevelAndUser(level, user)).thenReturn(Optional.empty());
        when(attitudeRepository.save(refEq(expectedNewAttitude))).thenReturn(expectedNewAttitude);

        final LevelAttitude result = service.setAttitude(USER_ID, LEVEL_ID, LevelAttitudeType.LIKE);

        assertSame(expectedNewAttitude, result);
    }

    @Test
    @DisplayName("deleteAttitude deletes when present")
    void deleteAttitudeDeletesWhenPresent() {
        when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

        final LevelAttitude existing = new LevelAttitude(user, level, LevelAttitudeType.LIKE);
        when(attitudeRepository.findByLevelAndUser(level, user)).thenReturn(Optional.of(existing));

        service.deleteAttitude(USER_ID, LEVEL_ID);

        verify(attitudeRepository).deleteByLevelAndUser(level, user);
    }

    @Test
    @DisplayName("deleteAttitude does nothing when absent")
    void deleteAttitudeNoopWhenAbsent() {
        when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
        when(attitudeRepository.findByLevelAndUser(level, user)).thenReturn(Optional.empty());

        service.deleteAttitude(USER_ID, LEVEL_ID);

        verify(attitudeRepository, never()).deleteByLevelAndUser(eq(level), eq(user));
    }

    @Test
    @DisplayName("count methods delegate to repository")
    void countDelegates() {
        when(attitudeRepository.countLikesByLevel(level)).thenReturn(5L);
        when(attitudeRepository.countDislikesByLevel(level)).thenReturn(2L);

        assertEquals(5L, service.countLikesByLevel(level));
        assertEquals(2L, service.countDislikesByLevel(level));
    }
}
