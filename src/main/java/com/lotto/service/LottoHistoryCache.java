package com.lotto.service;

import com.lotto.domain.LottoNumbers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 역대 당첨번호 Set 캐시 (SRP).
 *
 * <p>{@link LottoHistoryFetcher}는 fan-out 으로 1~N 회차를 모두 호출하므로,
 * 같은 {@code latestDraw} 에 대해 반복 호출되면 가상 스레드 작업 폭증과
 * 캐시 lookup 비용이 누적된다. 본 캐시는 결과 Set 자체를 보관해
 * 같은 회차 기준 요청을 단일 lookup 으로 단축한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LottoHistoryCache {

    private final LottoHistoryFetcher fetcher;

    /**
     * 캐시 키: latestDraw. 새로운 회차가 등장하면 자동으로 새 키가 되어 재계산된다.
     * TTL 은 {@code historyWinners} 캐시 정의(7일)를 따른다.
     */
    @Cacheable(cacheNames = "historyWinners", key = "#latestDraw")
    public Set<LottoNumbers> getOrFetch(int latestDraw) {
        log.info("역대 당첨번호 Set 캐시 미스 → fan-out 수집 (latestDraw={})", latestDraw);
        return fetcher.fetchAllWinners(latestDraw);
    }
}

