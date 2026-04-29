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

    /**
     * 게임 라벨 생성: A, B, ..., Z, AA, AB, ..., ZZ (총 26*27 = 702개 지원)
     * 동행복권 영수증 표기 규칙(A~AX 등)을 일반화.
     */
    static String labelFor(int index) {
        if (index < 0 || index >= 26 * 27) {
            throw new IllegalArgumentException("게임 라벨 생성 가능 범위(0~701)를 벗어났습니다: " + index);
        }
        if (index < 26) {
            return String.valueOf((char) ('A' + index));
        }
        int firstIdx = index / 26 - 1; // 26→AA(firstIdx=0), 52→BA(firstIdx=1), ...
        int secondIdx = index % 26;
        return "" + (char) ('A' + firstIdx) + (char) ('A' + secondIdx);
    }

    private final LottoService lottoService;
    private final LottoNumberGenerator lottoNumberGenerator;
    private final ReceiptNumberGenerator receiptNumberGenerator;
    private final LottoProperties properties;

    /**
     * 티켓을 발권한다.
     *
     * @param totalGames 총 게임 수 (예: 5)
     * @param manualLabelCount 수동 라벨로 표시할 게임 수 (총합 이하). null/음수면 0.
     *                         {@code manualNumbers}가 제공되면 그 크기로 강제된다.
     * @param manualNumbers 사용자가 직접 입력한 수동 번호 목록(앞쪽 슬롯에 배치). null 가능.
     * @param skipHistory true 면 역대 당첨번호 조회를 건너뛰고 단순 무작위 생성
     */
    public LottoTicket issue(int totalGames,
                             Integer manualLabelCount,
                             List<LottoNumbers> manualNumbers,
                             boolean skipHistory) {
        if (totalGames < 1) {
            throw new IllegalArgumentException("게임 수는 1 이상이어야 합니다.");
        }
        int max = properties.generator().maxCount();
        if (totalGames > max) {
            throw new IllegalArgumentException("게임 수는 " + max + " 이하여야 합니다.");
        }

        int manualSupplied = manualNumbers == null ? 0 : manualNumbers.size();
        if (manualSupplied > totalGames) {
            throw new IllegalArgumentException("수동 입력 번호 수가 총 게임 수를 초과했습니다.");
        }
        int manual = manualSupplied > 0
                ? manualSupplied
                : ((manualLabelCount == null || manualLabelCount < 0) ? 0 : manualLabelCount);
        if (manual > totalGames) {
            throw new IllegalArgumentException("수동 수는 총 게임 수를 초과할 수 없습니다.");
        }

        int autoCount = totalGames - manualSupplied;

        // 1) 자동 번호 생성 - 역대 당첨 회피 옵션
        List<LottoNumbers> autoPicks;
        int latestRound;
        if (autoCount == 0) {
            autoPicks = List.of();
            latestRound = 0;
        } else if (skipHistory) {
            autoPicks = new ArrayList<>(autoCount);
            for (int i = 0; i < autoCount; i++) autoPicks.add(lottoNumberGenerator.generate());
            latestRound = 0; // 미조회
        } else {
            var result = lottoService.generateUnique(autoCount);
            autoPicks = new ArrayList<>(result.generated());
            latestRound = result.latestDraw();
        }

        // 2) 슬롯 배치: 앞쪽 manualSupplied 게임은 사용자 입력, 뒤쪽은 자동
        List<LottoNumbers> picked = new ArrayList<>(totalGames);
        if (manualNumbers != null) picked.addAll(manualNumbers);
        picked.addAll(autoPicks);

        // 3) 게임 라벨/모드 부여 (앞쪽 manual 개를 수동 라벨로)
        List<TicketGame> games = new ArrayList<>(totalGames);
        for (int i = 0; i < totalGames; i++) {
            String label = labelFor(i);
            PickMode mode = (i < manual) ? PickMode.MANUAL : PickMode.AUTO;
            games.add(new TicketGame(label, mode, picked.get(i)));
        }

        // 4) 회차/일자
        LocalDateTime issuedAt = LocalDateTime.now().withNano(0);
        int nextRound = latestRound > 0 ? latestRound + 1 : 0;
        LocalDate drawDate = nextSaturday(issuedAt.toLocalDate());
        LocalDate claimDeadline = drawDate.plusDays(properties.ticket().claimValidityDays());

        // 5) 가격
        int unit = properties.ticket().pricePerGame();
        int total = unit * totalGames;

        // 6) 영수증 번호
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

    /** 기존 시그니처 호환 (수동 번호 미지원). */
    public LottoTicket issue(int totalGames, Integer manualGames, boolean skipHistory) {
        return issue(totalGames, manualGames, null, skipHistory);
    }

    private LocalDate nextSaturday(LocalDate base) {
        return base.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    }
}

