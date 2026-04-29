package com.lotto.domain;

/**
 * 티켓의 한 게임(A~E 등) 표현.
 */
public record TicketGame(
        String label,
        PickMode mode,
        LottoNumbers numbers
) {
    public TicketGame {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("게임 라벨은 비어 있을 수 없습니다.");
        }
        if (mode == null) {
            throw new IllegalArgumentException("게임 모드는 필수입니다.");
        }
        if (numbers == null) {
            throw new IllegalArgumentException("게임 번호는 필수입니다.");
        }
    }
}

