package com.gymlink.controller.member;

import com.gymlink.model.User;
import com.gymlink.service.AuthService;
import com.gymlink.service.RedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final AuthService authService;
    private final RedemptionService redemptionService;

    @GetMapping
    public String wallet(Authentication auth, Model model) {
        User user = authService.getByEmail(auth.getName());
        model.addAttribute("activeRedemptions", redemptionService.getActivePassesForMember(user));
        model.addAttribute("pastRedemptions", redemptionService.getPastPassesForMember(user));
        return "member/wallet";
    }
}