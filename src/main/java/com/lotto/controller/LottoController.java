package com.lotto.controller;

import com.lotto.config.LottoProperties;
import com.lotto.controller.dto.GenerateLottoResponse;
import com.lotto.controller.dto.TicketResponse;
import com.lotto.domain.LottoNumbers;
import com.lotto.service.LottoService;
import com.lotto.service.LottoTicketService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/lotto")
@RequiredArgsConstructor
@Validated
public class LottoController {

    private final LottoService lottoService;
    private final LottoTicketService lottoTicketService;
    private final LottoProperties properties;

    @GetMapping("/generate")
    public GenerateLottoResponse generateLotto(
            @RequestParam(required = false) @Min(1) @Max(50) Integer count
    ) {
        int requested = Optional.ofNullable(count).orElseGet(() -> properties.generator().defaultCount());
        var result = lottoService.generateUnique(requested);
        return new GenerateLottoResponse(
                result.latestDraw(),
                result.historicalWinnerCount(),
                result.generated().size(),
                result.generated().stream().map(LottoNumbers::numbers).toList(),
                result.generated().stream().map(LottoNumbers::toFormattedString).toList()
        );
    }

    @GetMapping("/ticket")
    public TicketResponse issueTicket(
            @RequestParam(required = false) @Min(1) @Max(50) Integer games,
            @RequestParam(required = false) @Min(0) @Max(50) Integer manual,
            @RequestParam(required = false, defaultValue = "true") boolean skipHistory
    ) {
        int total = Optional.ofNullable(games).orElseGet(() -> properties.generator().defaultCount());
        return TicketResponse.from(lottoTicketService.issue(total, manual, skipHistory));
    }
}
