package com.lotto.client;

import com.lotto.client.dto.LottoDrawResponse;
import com.lotto.config.LottoProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.Optional;

/**
 * 동행복권 공식 API 어댑터.
 * <ul>
 *   <li>SRP: HTTP 호출 책임만 담당</li>
 *   <li>Cache: 동일 회차는 영구 불변이므로 메모이즈 ({@code @Cacheable})</li>
 *   <li>Resilience: {@link RetryTemplate} 으로 네트워크 일시 장애에 지수 백오프 재시도</li>
 *   <li>Observability: Micrometer Timer 로 latency/실패 메트릭 수집</li>
 * </ul>
 *
 * <p>설계 메모: {@code @Cacheable}과 {@code @Retryable}을 같은 메서드에 함께 사용하면
 * AOP 어드바이스 순서가 환경에 따라 달라질 수 있다. 캐시가 항상 외곽에서 동작하도록
 * 재시도는 {@link RetryTemplate} 으로 명시적으로 감싼다.</p>
 */
@Slf4j
@Component
public class DhLotteryApiClient implements LottoApiClient {

    private final RestClient lottoRestClient;
    private final LottoProperties properties;
    private final RetryTemplate retryTemplate;
    private final Timer fetchTimer;
    private final Timer failTimer;

    public DhLotteryApiClient(RestClient lottoRestClient,
                              LottoProperties properties,
                              MeterRegistry meterRegistry) {
        this.lottoRestClient = lottoRestClient;
        this.properties = properties;
        this.retryTemplate = buildRetryTemplate(properties.api().retry());
        this.fetchTimer = Timer.builder("lotto.api.fetch")
                .description("동행복권 회차 조회 latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
        this.failTimer = Timer.builder("lotto.api.fail")
                .description("동행복권 회차 조회 실패")
                .register(meterRegistry);
    }

    private static RetryTemplate buildRetryTemplate(LottoProperties.Api.Retry retry) {
        RetryTemplate template = new RetryTemplate();
        // 네트워크/타임아웃만 재시도
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(retry.maxAttempts(),
                Map.of(ResourceAccessException.class, true), true);
        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(retry.initialInterval().toMillis());
        backOff.setMultiplier(retry.multiplier());
        backOff.setMaxInterval(retry.maxInterval().toMillis());
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOff);
        return template;
    }

    @Override
    @Cacheable(cacheNames = "lottoDraws", unless = "#result == null || !#result.isPresent()")
    public Optional<LottoDrawResponse> fetchDraw(int drawNo) {
        return retryTemplate.execute(
                ctx -> doFetch(drawNo),
                ctx -> {
                    log.error("회차 {} 재시도 모두 실패: {}", drawNo,
                            ctx.getLastThrowable() == null ? "?" : ctx.getLastThrowable().getMessage());
                    return Optional.empty();
                }
        );
    }

    private Optional<LottoDrawResponse> doFetch(int drawNo) {
        Timer.Sample sample = Timer.start();
        try {
            LottoDrawResponse response = lottoRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("method", properties.api().method())
                            .queryParam("drwNo", drawNo)
                            .build())
                    .retrieve()
                    .body(LottoDrawResponse.class);
            sample.stop(fetchTimer);
            if (response == null || !response.isSuccess()) {
                return Optional.empty();
            }
            return Optional.of(response);
        } catch (HttpClientErrorException.NotFound e) {
            // 404: 미존재 회차 → 정상 흐름. 재시도 금지.
            sample.stop(failTimer);
            return Optional.empty();
        } catch (RestClientResponseException e) {
            // 4xx/5xx: 응답 자체가 도달함 → 재시도해도 동일할 가능성 큼
            sample.stop(failTimer);
            log.warn("로또 회차 {} HTTP 오류: {}", drawNo, e.getStatusCode());
            return Optional.empty();
        } catch (ResourceAccessException e) {
            // 네트워크/타임아웃 → RetryTemplate 이 잡아 재시도
            sample.stop(failTimer);
            throw e;
        } catch (RestClientException e) {
            // 알 수 없는 클라이언트 측 예외
            sample.stop(failTimer);
            log.warn("로또 회차 {} 조회 실패: {}", drawNo, e.getMessage());
            return Optional.empty();
        }
    }
}



