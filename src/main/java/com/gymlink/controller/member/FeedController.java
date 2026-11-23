package com.gymlink.controller.member;

import com.gymlink.model.User;
import com.gymlink.service.AuthService;
import com.gymlink.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/member/feed")
@RequiredArgsConstructor
public class FeedController {

    private final AuthService authService;
    private final FeedService feedService;

    @GetMapping
    public String feed(Authentication auth, Model model) {
        User currentUser = authService.getByEmail(auth.getName());
        model.addAttribute("posts", feedService.getRecentPosts(currentUser));
        return "member/feed";
    }

    @PostMapping("/post")
    public String createPost(@RequestParam String content,
                             @RequestParam(required = false) String tag,
                             @RequestParam(required = false) String imageUrl,
                             @RequestParam(required = false) MultipartFile imageFile,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = authService.getByEmail(auth.getName());
            feedService.createPost(user, content, tag, imageUrl, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "¡Publicación compartida! Ganaste +25 XP.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/member/feed";
    }

    @PostMapping("/{id}/fire")
    public String toggleFire(@PathVariable Long id, Authentication auth) {
        User user = authService.getByEmail(auth.getName());
        feedService.toggleFire(user, id);
        return "redirect:/member/feed";
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String text,
                             Authentication auth) {
        User user = authService.getByEmail(auth.getName());
        if (text != null && !text.isBlank()) {
            feedService.addComment(user, id, text);
        }
        return "redirect:/member/feed";
    }
}