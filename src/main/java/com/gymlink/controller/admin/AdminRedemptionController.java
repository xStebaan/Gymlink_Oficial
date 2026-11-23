package com.gymlink.controller.admin;

import com.gymlink.service.RedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/redemptions")
@RequiredArgsConstructor
public class AdminRedemptionController {

    private final RedemptionService redemptionService;

    @GetMapping
    public String listRedemptions(Model model) {
        model.addAttribute("redemptions", redemptionService.getAllRedemptionsForAdmin());
        return "admin/redemptions";
    }

    @PostMapping("/{id}/deliver")
    public String deliver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        redemptionService.deliverPhysicalRedemption(id);
        redirectAttributes.addFlashAttribute("successMessage", "Entrega validada y marcada como ENTREGADO.");
        return "redirect:/admin/redemptions";
    }
}