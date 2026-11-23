package com.gymlink.controller.member;

import com.gymlink.dto.MemberHudDto;
import com.gymlink.model.User;
import com.gymlink.model.enums.VipRank;
import com.gymlink.service.AuthService;
import com.gymlink.service.ChallengeService;
import com.gymlink.service.GamificationService;
import com.gymlink.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberDashboardController {

    private final AuthService authService;
    private final GamificationService gamificationService;
    private final ChallengeService challengeService;
    private final RewardService rewardService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = authService.getByEmail(auth.getName());
        VipRank rank = user.getVipRank();

        long currentBaseXp = rank.getMinXp();
        long nextXp = rank.getNextRankXp();
        int rankPercent = (rank == VipRank.TITAN) ? 100
                : (int) Math.min(100, Math.max(0, ((user.getXp() - currentBaseXp) * 100) / Math.max(1, nextXp - currentBaseXp)));
        long xpRemaining = Math.max(0, nextXp - user.getXp());

        var targetReward = rewardService.getTargetRewardForDashboard();
        int targetCost = targetReward != null ? targetReward.pointsCost() : 1800;
        int rewardPercent = Math.min(100, (user.getPointsBalance() * 100) / Math.max(1, targetCost));
        int pointsToTarget = Math.max(0, targetCost - user.getPointsBalance());

        model.addAttribute("member", MemberHudDto.from(user));
        model.addAttribute("nextRankName", rank.getNextRankName());
        model.addAttribute("nextRankXp", nextXp);
        model.addAttribute("rankProgressPercent", rankPercent);
        model.addAttribute("xpRemaining", xpRemaining);
        model.addAttribute("weeklyDays", gamificationService.getWeeklyTracker(user));
        model.addAttribute("targetReward", targetReward);
        model.addAttribute("rewardProgressPercent", rewardPercent);
        model.addAttribute("pointsToTarget", pointsToTarget);
        model.addAttribute("missions", challengeService.getMissionsForMember(user));

        return "member/dashboard";
    }

    @PostMapping("/challenges/{id}/claim")
    public String claimChallenge(@PathVariable Long id,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = authService.getByEmail(auth.getName());
            challengeService.claimMission(user, id);
            redirectAttributes.addFlashAttribute("successMessage", "¡Recompensa de misión reclamada con éxito!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/member/dashboard";
    }
}