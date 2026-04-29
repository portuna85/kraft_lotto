package com.lotto.client;

import com.lotto.client.dto.LottoDrawResponse;
import com.lotto.config.LottoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * 동행복권 공식 API 어댑터.
 * - HTTP 호출 책임만 가짐 (SRP)
 * - 캐시: 같은 회차는 변하지 않으므로 메모이즈
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DhLotteryApiClient implements LottoApiClient {

    private final RestClient lottoRestClient;
    private final LottoProperties properties;

    @Override
    @Cacheable(cacheNames = "lottoDraws", unless = "#result == null || !#result.isPresent()")
    public Optional<LottoDrawResponse> fetchDraw(int drawNo) {
        try {
            LottoDrawResponse response = lottoRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("method", properties.api().method())
                            .queryParam("drwNo", drawNo)
                            .build())
                    .retrieve()
                    .body(LottoDrawResponse.class);
            if (response == null || !response.isSuccess()) {
                return Optional.empty();
            }
            return Optional.of(response);
        } catch (RestClientException e) {
            log.warn("로또 회차 {} 조회 실패: {}", drawNo, e.getMessage());
            return Optional.empty();
        }
    }
}

