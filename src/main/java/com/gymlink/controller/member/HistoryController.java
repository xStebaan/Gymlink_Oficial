package com.gymlink.controller.member;

import com.gymlink.model.User;
import com.gymlink.repository.PointTransactionRepository;
import com.gymlink.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/member/history")
@RequiredArgsConstructor
public class HistoryController {

    private final AuthService authService;
    private final PointTransactionRepository transactionRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public record PointTxViewDto(
            String createdAtFormatted,
            String concept,
            String type,
            boolean credit,
            int amount
    ) {}

    @GetMapping
    public String history(Authentication auth, Model model) {
        User user = authService.getByEmail(auth.getName());
        long totalEarned = transactionRepository.sumAmountByUserAndCredit(user, true);
        long totalRedeemed = transactionRepository.sumAmountByUserAndCredit(user, false);

        List<PointTxViewDto> txs = transactionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(t -> new PointTxViewDto(
                        t.getCreatedAt().format(FMT),
                        t.getConcept(),
                        t.getType().name(),
                        t.isCredit(),
                        t.getAmount()
                ))
                .toList();

        model.addAttribute("totalEarned", totalEarned);
        model.addAttribute("totalRedeemed", totalRedeemed);
        model.addAttribute("transactions", txs);
        return "member/history";
    }
}