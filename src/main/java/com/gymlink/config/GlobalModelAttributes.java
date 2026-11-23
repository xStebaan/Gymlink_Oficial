package com.gymlink.config;

import com.gymlink.dto.AdminKpiDto;
import com.gymlink.dto.MemberHudDto;
import com.gymlink.model.User;
import com.gymlink.model.enums.Role;
import com.gymlink.repository.UserRepository;
import com.gymlink.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final AdminDashboardService adminDashboardService;

    @ModelAttribute("currentMember")
    public MemberHudDto currentMember(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user != null && user.getRole() == Role.MEMBER) {
            return MemberHudDto.from(user);
        }
        return null;
    }

    @ModelAttribute("currentAdmin")
    public MemberHudDto currentAdmin(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user != null && user.getRole() == Role.ADMIN) {
            return MemberHudDto.from(user);
        }
        return null;
    }

    @ModelAttribute("quickStats")
    public AdminKpiDto quickStats(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user != null && user.getRole() == Role.ADMIN) {
            return adminDashboardService.getKpis();
        }
        return null;
    }

    private User resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName()).orElse(null);
    }
}