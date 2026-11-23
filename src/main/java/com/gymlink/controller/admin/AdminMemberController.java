package com.gymlink.controller.admin;

import com.gymlink.dto.MemberHudDto;
import com.gymlink.model.User;
import com.gymlink.model.enums.Role;
import com.gymlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final UserRepository userRepository;

    @GetMapping
    public String listMembers(Model model) {
        List<MemberHudDto> members = userRepository.findByRoleOrderByFullNameAsc(Role.MEMBER)
                .stream()
                .map(MemberHudDto::from)
                .toList();
        model.addAttribute("members", members);
        return "admin/members";
    }

    @PostMapping("/{id}/shield")
    public String grantShield(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User member = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atleta no encontrado"));
        member.setShields(member.getShields() + 1);
        userRepository.save(member);
        redirectAttributes.addFlashAttribute("successMessage",
                "Se otorgó +1 Escudo Protector a " + member.getFullName() + ".");
        return "redirect:/admin/members";
    }
}