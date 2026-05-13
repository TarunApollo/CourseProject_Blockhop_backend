package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

/// Data transfer object for level like/dislike counts.
public record LikeDislikeCountDTO(
        long likes,
        long dislikes) {
}
