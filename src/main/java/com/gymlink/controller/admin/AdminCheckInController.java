package com.gymlink.controller.admin;

import com.gymlink.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/checkin")
@RequiredArgsConstructor
public class AdminCheckInController {

    private final CheckInService checkInService;

    @GetMapping
    public String checkinTerminal(@RequestParam(required = false) String query, Model model) {
        model.addAttribute("query", query);
        model.addAttribute("foundMember", checkInService.findFirstMemberForCheckIn(query));
        model.addAttribute("todayCheckins", checkInService.getTodayCheckIns());
        return "admin/checkin";
    }

    @PostMapping
    public String registerCheckIn(@RequestParam Long memberId,
                                  RedirectAttributes redirectAttributes) {
        try {
            int awarded = checkInService.registerCheckIn(memberId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Check-in registrado! Se sumaron +" + awarded + " PTS al atleta.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/checkin";
    }
}