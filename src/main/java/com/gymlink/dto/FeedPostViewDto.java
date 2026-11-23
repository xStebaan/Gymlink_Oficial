package com.gymlink.dto;

import java.util.List;

public record FeedPostViewDto(
        Long id,
        String authorName,
        String authorInitials,
        String authorRank,
        long authorXp,
        int authorStreak,
        int authorCheckins,
        String authorBio,
        String content,
        String tag,
        String imageUrl,
        boolean systemAchievement,
        String timeAgo,
        int fireCount,
        boolean firedByMe,
        List<CommentViewDto> comments
) {
    public record CommentViewDto(
            String authorName,
            String authorInitials,
            String text,
            String timeAgo
    ) {}
}