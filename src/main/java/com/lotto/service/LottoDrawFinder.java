package com.lotto.service;

import com.lotto.client.LottoApiClient;
import com.lotto.config.LottoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 최신 회차 번호를 이진 탐색으로 찾는다 (SRP).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LottoDrawFinder {

    private final LottoApiClient lottoApiClient;
    private final LottoProperties properties;

    public int findLatestDraw() {
        int step = properties.draw().searchStep();
        int low = properties.draw().searchStart();
        int high = low + step;

        // 1) 상한 확장
        while (isValid(high)) {
            low = high;
            high += step;
        }
        // 2) 이진 탐색
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

