package com.lotto.service;

import com.lotto.config.LottoProperties;
import com.lotto.domain.LottoNumberGenerator;
import com.lotto.domain.LottoNumbers;
import com.lotto.domain.LottoTicket;
import com.lotto.domain.PickMode;
import com.lotto.domain.ReceiptNumberGenerator;
import com.lotto.domain.TicketGame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * 영수증(티켓) 생성 서비스 (SRP).
 * - 게임별 수동/자동 모드 결정
 * - 회차/일자/영수증번호/합계 계산
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LottoTicketService {

    private static final String[] GAME_LABELS = buildLabels();

    private static String[] buildLabels() {
        String[] labels = new String[50];
        for (int i = 0; i < 26; i++) {
            labels[i] = String.valueOf((char) ('A' + i));
        }
        for (int i = 26; i < 50; i++) {
            labels[i] = "A" + (char) ('A' + (i - 26));
        }
        return labels;
    }

    private final LottoService lottoService;
    private final LottoNumberGenerator lottoNumberGenerator;
    private final ReceiptNumberGenerator receiptNumberGenerator;
    private final LottoProperties properties;

    /**
     * 티켓을 발권한다.
     *
     * @param totalGames 총 게임 수 (예: 5)
     * @param manualGames 수동 게임 수 (총합 이하). null/음수면 0
     * @param skipHistory true 면 역대 당첨번호 조회를 건너뛰고 단순 무작위 생성
     */
    public LottoTicket issue(int totalGames, Integer manualGames, boolean skipHistory) {
        if (totalGames < 1) {
            throw new IllegalArgumentException("게임 수는 1 이상이어야 합니다.");
        }
        int max = properties.generator().maxCount();
        if (totalGames > max) {
            throw new IllegalArgumentException("게임 수는 " + max + " 이하여야 합니다.");
        }
        int manual = (manualGames == null || manualGames < 0) ? 0 : manualGames;
        if (manual > totalGames) {
            throw new IllegalArgumentException("수동 수는 총 게임 수를 초과할 수 없습니다.");
        }

        // 1) 번호 생성 - 역대 당첨 회피 옵션
        List<LottoNumbers> picked;
        int latestRound;
        if (skipHistory) {
            picked = new ArrayList<>(totalGames);
            for (int i = 0; i < totalGames; i++) picked.add(lottoNumberGenerator.generate());
            latestRound = 0; // 미조회
        } else {
            var result = lottoService.generateUnique(totalGames);
            picked = new ArrayList<>(result.generated());
            latestRound = result.latestDraw();
        }

        // 2) 게임 라벨/모드 부여 (앞쪽 manual 개를 수동으로)
        List<TicketGame> games = new ArrayList<>(totalGames);
        for (int i = 0; i < totalGames; i++) {
            String label = GAME_LABELS[i];
            PickMode mode = (i < manual) ? PickMode.MANUAL : PickMode.AUTO;
            games.add(new TicketGame(label, mode, picked.get(i)));
        }

        // 3) 회차/일자
        LocalDateTime issuedAt = LocalDateTime.now().withNano(0);
        int nextRound = latestRound > 0 ? latestRound + 1 : 0;
        LocalDate drawDate = nextSaturday(issuedAt.toLocalDate());
        LocalDate claimDeadline = drawDate.plusDays(properties.ticket().claimValidityDays());

        // 4) 가격
        int unit = properties.ticket().pricePerGame();
        int total = unit * totalGames;

        // 5) 영수증 번호
        String receipt = receiptNumberGenerator.generate();

        return new LottoTicket(
                nextRound,
                issuedAt,
                drawDate,
                claimDeadline,
                receipt,
                games,
                unit,
                total
        );
    }

    private LocalDate nextSaturday(LocalDate base) {
        return base.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    }
}

