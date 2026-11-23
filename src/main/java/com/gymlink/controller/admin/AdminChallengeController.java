package com.gymlink.controller.admin;

import com.gymlink.model.enums.ChallengeFrequency;
import com.gymlink.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/challenges")
@RequiredArgsConstructor
public class AdminChallengeController {

    private final ChallengeService challengeService;

    @GetMapping
    public String listChallenges(Model model) {
        model.addAttribute("challenges", challengeService.getAllChallengesForAdmin());
        return "admin/challenges";
    }

    @PostMapping("/save")
    public String saveChallenge(@RequestParam String title,
                                @RequestParam String description,
                                @RequestParam ChallengeFrequency frequency,
                                @RequestParam int targetCount,
                                @RequestParam int pointsReward,
                                @RequestParam int xpReward,
                                RedirectAttributes redirectAttributes) {
        challengeService.saveChallenge(title, description, frequency, targetCount, pointsReward, xpReward);
        redirectAttributes.addFlashAttribute("successMessage", "¡Nuevo reto lanzado para todos los atletas!");
        return "redirect:/admin/challenges";
    }

    @PostMapping("/{id}/toggle")
    public String toggleChallenge(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        challengeService.toggleChallenge(id);
        redirectAttributes.addFlashAttribute("successMessage", "Estado del reto actualizado.");
        return "redirect:/admin/challenges";
    }
}