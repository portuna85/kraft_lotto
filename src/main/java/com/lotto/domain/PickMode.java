package com.lotto.domain;

/**
 * 게임 선택 모드.
 *  - MANUAL: 수동
 *  - AUTO  : 자동
 */
public enum PickMode {
    MANUAL("수동"),
    AUTO("자동");

    private final String label;

    PickMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

