package com.gymlink.controller.admin;

import com.gymlink.model.enums.RewardCategory;
import com.gymlink.model.enums.RewardType;
import com.gymlink.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rewards")
@RequiredArgsConstructor
public class AdminRewardController {

    private final RewardService rewardService;

    @GetMapping
    public String listRewards(Model model) {
        model.addAttribute("rewards", rewardService.getAllRewardsForAdmin());
        return "admin/rewards";
    }

    @PostMapping("/save")
    public String saveReward(@RequestParam(required = false) Long id,
                             @RequestParam String name,
                             @RequestParam String description,
                             @RequestParam RewardCategory category,
                             @RequestParam RewardType type,
                             @RequestParam int pointsCost,
                             @RequestParam int stock,
                             RedirectAttributes redirectAttributes) {
        rewardService.saveReward(id, name, description, category, type, pointsCost, stock);
        redirectAttributes.addFlashAttribute("successMessage", "Premio VIP guardado correctamente.");
        return "redirect:/admin/rewards";
    }

    @PostMapping("/{id}/toggle")
    public String toggleReward(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        rewardService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMessage", "Estado del premio actualizado.");
        return "redirect:/admin/rewards";
    }
}