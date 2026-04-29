package com.lotto.service;

import com.lotto.client.LottoApiClient;
import com.lotto.config.LottoProperties;
import com.lotto.domain.LottoNumbers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

/**
 * 1회차부터 최신 회차까지 당첨번호를 모두 가져와 Set 으로 반환 (SRP).
 * Java 21 Virtual Threads 기반 fan-out. Semaphore 로 외부 API 동시 요청 수 제한.
 *
 * <p>동시성 한도는 {@code lotto.api.max-concurrent} 설정으로 외부화되어
 * 동행복권 서버 보호 정책 변경 시 재배포 없이 조정 가능하다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LottoHistoryFetcher {

    private final LottoApiClient lottoApiClient;
    private final LottoProperties properties;

    public Set<LottoNumbers> fetchAllWinners(int latestDraw) {
        if (latestDraw <= 0) {
            return Set.of();
        }
        Set<LottoNumbers> winners = ConcurrentHashMap.newKeySet();
        Semaphore semaphore = new Semaphore(properties.api().maxConcurrent());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.rangeClosed(1, latestDraw).forEach(no ->
                executor.execute(() -> {
                    try {
                        semaphore.acquire();
                        try {
                            lottoApiClient.fetchDraw(no).ifPresentOrElse(
                                r -> winners.add(new LottoNumbers(r.winningNumbers())),
                                () -> log.debug("회차 {} 응답 없음 (스킵)", no)
                            );
                        } finally {
                            semaphore.release();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("회차 {} 처리 중단", no);
                    }
                })
            );
        }

        log.info("수집된 회차 수: {}/{}", winners.size(), latestDraw);
        return Set.copyOf(winners);
    }
}
