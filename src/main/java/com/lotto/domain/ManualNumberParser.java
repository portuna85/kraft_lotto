package com.lotto.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 사용자 입력 수동 번호 문자열을 {@link LottoNumbers} 도메인 객체로 변환하는 유틸리티.
 * <p>실제 토큰 파싱/검증은 {@link LottoNumbers#parse(String)} 에 위임하고,
 * 본 클래스는 입력 컬렉션 차원의 null/blank 처리만 담당한다.</p>
 */
public final class ManualNumberParser {

    private ManualNumberParser() {
    }

    /**
     * 입력 문자열 목록을 {@link LottoNumbers} 목록으로 파싱한다.
     * null/공백 항목은 건너뛰고, null 입력은 빈 목록을 반환한다.
     */
    public static List<LottoNumbers> parse(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<LottoNumbers> parsed = new ArrayList<>(raw.size());
        for (String entry : raw) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            parsed.add(LottoNumbers.parse(entry));
        }
        return parsed;
    }
}
