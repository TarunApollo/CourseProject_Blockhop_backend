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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.Mockito;

import java.util.Optional;

@SpringBootTest(classes = LevelAttitudeService.class)
@DisplayName("The Level Attitude Service")
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
        Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class,
                () -> service.setAttitude(USER_ID, LEVEL_ID, LevelAttitudeType.LIKE));
    }

    @Test
    @DisplayName("setAttitude throws when level missing")
    void setAttitudeLevelMissing() {
        Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

        Assertions.assertThrows(LevelNotFoundException.class,
                () -> service.setAttitude(USER_ID, LEVEL_ID, LevelAttitudeType.LIKE));
    }

    @Test
    @DisplayName("setAttitude updates existing attitude")
    void setAttitudeUpdatesExisting() {
        Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

        final LevelAttitude existing = new LevelAttitude(user, level, LevelAttitudeType.DISLIKE);
        Mockito.when(attitudeRepository.findByLevelAndUser(level, user)).thenReturn(Optional.of(existing));
        Mockito.when(attitudeRepository.save(existing)).thenAnswer(i -> i.getArgument(0));

        final LevelAttitude result = service.setAttitude(USER_ID, LEVEL_ID, LevelAttitudeType.LIKE);

        Assertions.assertEquals(LevelAttitudeType.LIKE, result.getAttitude());
        Mockito.verify(attitudeRepository).save(existing);
    }

    @Test
    @DisplayName("setAttitude creates new attitude when none exists")
    void setAttitudeCreatesNew() {
        Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
        Mockito.when(attitudeRepository.findByLevelAndUser(level, user)).thenReturn(Optional.empty());
        Mockito.when(attitudeRepository.save(Mockito.refEq(expectedNewAttitude))).thenReturn(expectedNewAttitude);

        final LevelAttitude result = service.setAttitude(USER_ID, LEVEL_ID, LevelAttitudeType.LIKE);

        Assertions.assertSame(expectedNewAttitude, result);
    }

    @Test
    @DisplayName("deleteAttitude deletes when present")
    void deleteAttitudeDeletesWhenPresent() {
        Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

        final LevelAttitude existing = new LevelAttitude(user, level, LevelAttitudeType.LIKE);
        Mockito.when(attitudeRepository.findByLevelAndUser(level, user)).thenReturn(Optional.of(existing));

        service.deleteAttitude(USER_ID, LEVEL_ID);

        Mockito.verify(attitudeRepository).deleteByLevelAndUser(level, user);
    }

    @Test
    @DisplayName("deleteAttitude does nothing when absent")
    void deleteAttitudeNoopWhenAbsent() {
        Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
        Mockito.when(attitudeRepository.findByLevelAndUser(level, user)).thenReturn(Optional.empty());

        service.deleteAttitude(USER_ID, LEVEL_ID);

        Mockito.verify(attitudeRepository, Mockito.never()).deleteByLevelAndUser(level, user);
    }

    @Test
    @DisplayName("count methods delegate to repository")
    void countDelegates() {
        Mockito.when(attitudeRepository.countLikesByLevel(level)).thenReturn(5L);
        Mockito.when(attitudeRepository.countDislikesByLevel(level)).thenReturn(2L);

        Assertions.assertEquals(5L, service.countLikesByLevel(level));
        Assertions.assertEquals(2L, service.countDislikesByLevel(level));
    }
}
