package com.lotto.domain;

/**
 * 티켓의 한 게임(A~E 등) 표현.
 */
public record TicketGame(
        String label,
        PickMode mode,
        LottoNumbers numbers
) {
    /** 라벨 생성 가능한 인덱스 상한(0-based, 반열림). A~Z + AA~ZZ = 26 + 26*26 = 702. */
    public static final int MAX_LABEL_INDEX = 26 + 26 * 26;

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

    /**
     * 게임 라벨 생성: A, B, ..., Z, AA, AB, ..., ZZ (총 26 + 26*26 = 702개 지원).
     * 동행복권 영수증 표기 규칙(A~AX 등)을 일반화한 결과이다.
     */
    public static String labelFor(int index) {
        if (index < 0 || index >= MAX_LABEL_INDEX) {
            throw new IllegalArgumentException(
                    "게임 라벨 생성 가능 범위(0~" + (MAX_LABEL_INDEX - 1) + ")를 벗어났습니다: " + index);
        }
        if (index < 26) {
            return String.valueOf((char) ('A' + index));
        }
        int firstIdx = index / 26 - 1; // 26→AA(firstIdx=0), 52→BA(firstIdx=1), ...
        int secondIdx = index % 26;
        return "" + (char) ('A' + firstIdx) + (char) ('A' + secondIdx);
    }
}

