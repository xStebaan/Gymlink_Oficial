package com.gymlink.controller.member;

import com.gymlink.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public String leaderboard(Model model) {
        model.addAttribute("topAthletes", leaderboardService.getTop10Athletes());
        return "member/leaderboard";
    }
}