package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelAggregationService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/// REST endpoints producing aggregated views over published levels.
@RestController
@RequestMapping("/levels")
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed singleton; injected services are intentionally shared")
public class LevelAggregationController {

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
    /// @return a list of published levels sorted by the specified criteria
    @GetMapping("/published")
    public List<LevelSummaryDto> getPublishedLevels(
            @RequestParam final PublishedLevelSortBy sortBy,
            @RequestParam(defaultValue = "ALL_TIME") final DateRangePreset period,
            final Authentication authentication) {
        final String currentUserId = resolveCurrentUserId(authentication);
        return this.levelAggregationService.getPublishedLevels(sortBy, period, currentUserId);
    }

    private @Nullable String resolveCurrentUserId(final Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        try {
            return AuthUtils.getUserIdFromAuth(authentication);
        } catch (ResponseStatusException ignored) {
            return null;
        }
    }
}
