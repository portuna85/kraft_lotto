package com.lotto.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 로또 영수증(티켓) 도메인 객체.
 */
public record LottoTicket(
        int round,
        LocalDateTime issuedAt,
        LocalDate drawDate,
        LocalDate claimDeadline,
        String receiptNumber,
        List<TicketGame> games,
        int pricePerGame,
        int totalPrice
) {
    public LottoTicket {
        if (games == null || games.isEmpty()) {
            throw new IllegalArgumentException("게임은 1개 이상이어야 합니다.");
        }
        if (pricePerGame <= 0) {
            throw new IllegalArgumentException("게임 단가는 0보다 커야 합니다.");
        }
        games = List.copyOf(games);
    }
}

