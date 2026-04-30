package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPublishService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

///
 /// Black-box tests for LevelPublishController.
 /// Verifies endpoints for publishing and unpublishing levels.
 ///
@WebMvcTest(controllers = LevelPublishController.class)
@AutoConfigureRestTestClient
@Import(ControllerSecurityTestConfig.class)
@DisplayName("Level Publish Controller Logic Tests")
class LevelPublishControllerTests {

    /// A level ID used across tests.
    private static final String LEVEL_ID = "level-1";

    /// The mocked publish service.
    @MockitoBean
    private LevelPublishService levelPublishService;

    /// The RestTestClient for performing requests.
    @Autowired
    private RestTestClient restTestClient;

    /// Verifies that publishing a level returns 204 No Content. ///
    @Test
    @DisplayName("PUT /levels/{id}/publish should return 204")
    void testPublishLevel() {
        Mockito.doNothing().when(levelPublishService)
                .publish(ControllerSecurityTestConfig.DEFAULT_USER_ID, LEVEL_ID);

        final HttpStatusCode status = restTestClient.put()
                .uri("/levels/{levelId}/publish", LEVEL_ID)
                .exchange()
                .returnResult(Void.class)
                .getStatus();
        Assertions.assertEquals(HttpStatus.NO_CONTENT, status);
    }

    /// Verifies that unpublishing a level returns 204 No Content. ///
    @Test
    @DisplayName("PUT /levels/{id}/unpublish should return 204")
    void testUnpublishLevel() {
        Mockito.doNothing().when(levelPublishService)
                .unpublishLevel(ControllerSecurityTestConfig.DEFAULT_USER_ID, LEVEL_ID);

        final HttpStatusCode status = restTestClient.put()
                .uri("/levels/{levelId}/unpublish", LEVEL_ID)
                .exchange()
                .returnResult(Void.class)
                .getStatus();
        Assertions.assertEquals(HttpStatus.NO_CONTENT, status);
    }
}
