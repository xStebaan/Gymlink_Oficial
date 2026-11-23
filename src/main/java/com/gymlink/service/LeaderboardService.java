package com.gymlink.service;

import com.gymlink.dto.MemberHudDto;
import com.gymlink.model.enums.Role;
import com.gymlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;

    public List<MemberHudDto> getTop10Athletes() {
        return userRepository.findTop10ByRoleOrderByXpDesc(Role.MEMBER)
                .stream()
                .map(MemberHudDto::from)
                .toList();
    }
}