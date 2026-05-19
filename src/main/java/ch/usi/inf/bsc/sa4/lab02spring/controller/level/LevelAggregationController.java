package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PublishedLevelSearchCriteria;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelAggregationService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.OAuth2UserUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/// REST endpoints producing aggregated views over published levels.
@RestController
@RequestMapping("/levels")
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed singleton; injected services are intentionally shared")
public class LevelAggregationController {
    /// OAuth2 subject attribute name.
    private static final String OAUTH_SUB_ATTRIBUTE = "sub";

    /// Produces sorted summaries for published levels.
    private final LevelAggregationService levelAggregationService;

    /// Constructs a new LevelAggregationController.
    /// 
    /// @param levelAggregationService the service for published level summaries
    @Autowired
    public LevelAggregationController(final LevelAggregationService levelAggregationService) {
        this.levelAggregationService = levelAggregationService;
    }

    /// Returns all published levels as summaries, sorted by the given criteria.
    ///
    /// @param sortBy sorting strategy (required): CLEAR_RATE or POPULARITY
    /// @param period time range for popularity calculation (optional, default
    ///               ALL_TIME): ALL_TIME, TODAY, LAST_7_DAYS, LAST_30_DAYS,
    ///               LAST_365_DAYS. Only relevant when sortBy is POPULARITY; ignored
    ///               for CLEAR_RATE.
    /// @param criteria optional numeric search filters
    /// @param oauth2User the authenticated OAuth2 user
    /// @return a list of published levels sorted by the specified criteria
    @GetMapping("/published")
    public List<LevelSummaryDto> getPublishedLevels(
            @RequestParam final PublishedLevelSortBy sortBy,
            @RequestParam(defaultValue = "ALL_TIME") final DateRangePreset period,
            @ModelAttribute final PublishedLevelSearchCriteria criteria,
            @AuthenticationPrincipal final OAuth2User oauth2User) {
        final String currentUserId = OAuth2UserUtils.getRequiredAttribute(oauth2User, OAUTH_SUB_ATTRIBUTE);
        return this.levelAggregationService.getPublishedLevels(sortBy, period, criteria, currentUserId);
    }

    /// Returns a random published level from the same result set produced by the
    /// published-level search query.
    ///
    /// @param sortBy sorting strategy (required): CLEAR_RATE or POPULARITY
    /// @param period time range for popularity calculation (optional, default
    ///               ALL_TIME)
    /// @param criteria optional numeric search filters
    /// @param oauth2User the authenticated OAuth2 user
    /// @return 200 OK with a random matching level, or 404 when no levels match
    @GetMapping("/published/random")
    public ResponseEntity<LevelSummaryDto> getRandomPublishedLevel(
            @RequestParam final PublishedLevelSortBy sortBy,
            @RequestParam(defaultValue = "ALL_TIME") final DateRangePreset period,
            @ModelAttribute final PublishedLevelSearchCriteria criteria,
            @AuthenticationPrincipal final OAuth2User oauth2User) {
        final String currentUserId = OAuth2UserUtils.getRequiredAttribute(oauth2User, OAUTH_SUB_ATTRIBUTE);
        return this.levelAggregationService.getRandomPublishedLevel(sortBy, period, criteria, currentUserId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
