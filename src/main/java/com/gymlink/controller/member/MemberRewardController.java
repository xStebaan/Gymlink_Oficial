package com.gymlink.controller.member;

import com.gymlink.model.Redemption;
import com.gymlink.model.User;
import com.gymlink.service.AuthService;
import com.gymlink.service.RedemptionService;
import com.gymlink.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/member/rewards")
@RequiredArgsConstructor
public class MemberRewardController {

    private final AuthService authService;
    private final RewardService rewardService;
    private final RedemptionService redemptionService;

    @GetMapping
    public String shop(Model model) {
        model.addAttribute("rewards", rewardService.getActiveRewardsForShop());
        return "member/rewards";
    }

    @PostMapping("/redeem")
    public String redeem(@RequestParam Long rewardId,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) {
        try {
            User user = authService.getByEmail(auth.getName());
            Redemption redemption = redemptionService.redeemReward(user, rewardId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Canje exitoso! Código " + redemption.getCode() + " disponible en Mi Billetera.");
            return "redirect:/member/wallet";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/member/rewards";
        }
    }
}