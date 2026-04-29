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

import java.util.ArrayList;
import java.util.List;
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

    /**
     * 영수증(티켓) 발권.
     *
     * @param games 총 게임 수
     * @param manual 수동 라벨 게임 수(수동 번호 직접 입력 시 무시되고 입력 수가 우선)
     * @param manualNumbers 수동 입력 번호 목록. 각 항목은 6개 정수 콤마/공백 구분.
     *                      예) {@code ?manualNumbers=1,2,3,4,5,6&manualNumbers=7,8,9,10,11,12}
     * @param skipHistory true(기본) 면 역대 당첨 미조회로 빠른 발권
     */
    @GetMapping("/ticket")
    public TicketResponse issueTicket(
            @RequestParam(required = false) @Min(1) @Max(50) Integer games,
            @RequestParam(required = false) @Min(0) @Max(50) Integer manual,
            @RequestParam(required = false) List<String> manualNumbers,
            @RequestParam(required = false, defaultValue = "true") boolean skipHistory
    ) {
        int total = Optional.ofNullable(games).orElseGet(() -> properties.generator().defaultCount());
        List<LottoNumbers> manualParsed = parseManualNumbers(manualNumbers);
        return TicketResponse.from(
                lottoTicketService.issue(total, manual, manualParsed, skipHistory)
        );
    }

    /** 입력된 수동 번호 문자열들을 도메인 객체로 변환한다. {@link LottoNumbers#parse}에 위임. */
    private static List<LottoNumbers> parseManualNumbers(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        List<LottoNumbers> parsed = new ArrayList<>(raw.size());
        for (String entry : raw) {
            if (entry == null || entry.isBlank()) continue;
            parsed.add(LottoNumbers.parse(entry));
        }
        return parsed;
    }
}
