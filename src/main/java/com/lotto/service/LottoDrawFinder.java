package com.lotto.service;

import com.lotto.client.LottoApiClient;
import com.lotto.config.LottoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * 최신 회차 번호를 이진 탐색으로 찾는다 (SRP).
 * 결과는 30분간 캐시 — 매주 토요일 추첨 주기에 맞춰 불필요한 반복 탐색을 방지.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LottoDrawFinder {

    private final LottoApiClient lottoApiClient;
    private final LottoProperties properties;

    @Cacheable(cacheNames = "latestDraw")
    public int findLatestDraw() {
        int low = properties.draw().searchStart();
        int span = properties.draw().searchStep();
        int high = low + span;

        // 1) 상한을 지수적(2배)으로 확장 → O(log n) 호출로 첫 미존재 회차 발견.
        //    예: searchStart=1100, searchStep=100 → 시도 회차 1200, 1400, 1800, 2600, ...
        while (isValid(high)) {
            low = high;
            span <<= 1;            // 2배 확장
            high = low + span;
        }

        // 2) [low, high] 구간을 이진 탐색으로 좁혀 마지막 유효 회차 확정
        while (low + 1 < high) {
            int mid = low + (high - low) / 2;
            if (isValid(mid)) {
                low = mid;
            } else {
                high = mid;
            }
        }
        log.info("최신 회차: {}", low);
        return low;
    }

    private boolean isValid(int drawNo) {
        return lottoApiClient.fetchDraw(drawNo).isPresent();
    }
}
