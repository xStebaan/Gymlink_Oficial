package com.gymlink.service;

import com.gymlink.dto.FeedPostViewDto;
import com.gymlink.model.FeedPost;
import com.gymlink.model.PostComment;
import com.gymlink.model.PostFire;
import com.gymlink.model.User;
import com.gymlink.model.enums.TransactionType;
import com.gymlink.repository.FeedPostRepository;
import com.gymlink.repository.PostCommentRepository;
import com.gymlink.repository.PostFireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedPostRepository feedPostRepository;
    private final PostCommentRepository commentRepository;
    private final PostFireRepository fireRepository;
    private final GamificationService gamificationService;

    @Value("${gymlink.uploads.dir:uploads}")
    private String uploadsDir;

    @Transactional(readOnly = true)
    public List<FeedPostViewDto> getRecentPosts(User currentUser) {
        return feedPostRepository.findTop30ByOrderByCreatedAtDesc()
                .stream()
                .map(post -> toDto(post, currentUser))
                .toList();
    }

    @Transactional
    public void createPost(User author, String content, String tag,
                           String imageUrl, MultipartFile imageFile) {
        String finalImageUrl = (imageUrl != null && !imageUrl.isBlank()) ? imageUrl.trim() : null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Path dir = Paths.get(uploadsDir).toAbsolutePath().normalize();
                Files.createDirectories(dir);
                String ext = Optional.ofNullable(imageFile.getOriginalFilename())
                        .filter(f -> f.contains("."))
                        .map(f -> f.substring(f.lastIndexOf('.')))
                        .orElse(".jpg");
                String filename = UUID.randomUUID() + ext;
                Files.copy(imageFile.getInputStream(), dir.resolve(filename));
                finalImageUrl = "/uploads/" + filename;
            } catch (IOException e) {
                throw new RuntimeException("No se pudo guardar la imagen de la publicación.", e);
            }
        }

        feedPostRepository.save(FeedPost.builder()
                .author(author)
                .content(content.trim())
                .tag((tag == null || tag.isBlank()) ? "#Fuerza" : tag.trim())
                .imageUrl(finalImageUrl)
                .systemAchievement(false)
                .build());

        gamificationService.grantBonus(author, 0, 25, "Publicación en Club Social", TransactionType.SOCIAL);
    }

    @Transactional
    public void toggleFire(User user, Long postId) {
        FeedPost post = feedPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada"));

        Optional<PostFire> existing = fireRepository.findByPostAndUser(post, user);
        if (existing.isPresent()) {
            fireRepository.delete(existing.get());
        } else {
            fireRepository.save(PostFire.builder().post(post).user(user).build());
            if (!post.getAuthor().getId().equals(user.getId())) {
                gamificationService.grantBonus(post.getAuthor(), 5, 0,
                        "Fuego recibido en Club Social", TransactionType.SOCIAL);
            }
        }
    }

    @Transactional
    public void addComment(User author, Long postId, String text) {
        FeedPost post = feedPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada"));

        commentRepository.save(PostComment.builder()
                .post(post)
                .author(author)
                .text(text.trim())
                .build());
    }

    private FeedPostViewDto toDto(FeedPost post, User currentUser) {
        User a = post.getAuthor();
        boolean firedByMe = currentUser != null && fireRepository.existsByPostAndUser(post, currentUser);

        List<FeedPostViewDto.CommentViewDto> comments = post.getComments().stream()
                .map(c -> new FeedPostViewDto.CommentViewDto(
                        c.getAuthor().getFullName(),
                        c.getAuthor().getInitials(),
                        c.getText(),
                        formatTimeAgo(c.getCreatedAt())
                ))
                .toList();

        return new FeedPostViewDto(
                post.getId(),
                a.getFullName(),
                a.getInitials(),
                a.getVipRank().name(),
                a.getXp(),
                a.getStreakDays(),
                a.getTotalCheckins(),
                a.getBio(),
                post.getContent(),
                post.getTag(),
                post.getImageUrl(),
                post.isSystemAchievement(),
                formatTimeAgo(post.getCreatedAt()),
                post.getFires().size(),
                firedByMe,
                comments
        );
    }

    private String formatTimeAgo(LocalDateTime created) {
        long mins = Math.max(1, Duration.between(created, LocalDateTime.now()).toMinutes());
        if (mins < 60) return "Hace " + mins + " min";
        long hours = mins / 60;
        if (hours < 24) return "Hace " + hours + " h";
        return "Hace " + (hours / 24) + " d";
    }
}