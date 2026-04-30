package com.lotto.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "lotto")
@Validated
public record LottoProperties(
        @Valid @NotNull Api api,
        @Valid @NotNull Draw draw,
        @Valid @NotNull Generator generator,
        @Valid @NotNull Ticket ticket,
        @Valid @NotNull Cache cache
) {
    public record Api(
            @NotBlank String baseUrl,
            @NotBlank String method,
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout,
            @Min(1) int maxConcurrent,
            @Valid @NotNull Retry retry
    ) {
        public record Retry(
                @Min(1) int maxAttempts,
                @NotNull Duration initialInterval,
                @Min(1) double multiplier,
                @NotNull Duration maxInterval
        ) {}
    }

    public record Draw(
            @Min(1) int searchStart,
            @Min(1) int searchStep
    ) {}

    public record Generator(
            @Min(1) int defaultCount,
            @Min(1) int maxCount,
            @Min(1) int numberMin,
            @Min(1) int numberMax,
            @Min(1) int pickSize,
            @Min(1) int safetyMultiplier
    ) {}

    public record Ticket(
            @Min(100) int pricePerGame,
            @Min(1) int claimValidityDays
    ) {}

    /**
     * Caffeine 캐시 사이즈/TTL 외부화. 각 캐시는 {@code RestClientConfig} 에서 등록되며
     * 본 프로퍼티가 단일 진실 소스(SSoT)로서 운영 중 코드 수정 없이 튜닝 가능하다.
     */
    public record Cache(
            @Valid @NotNull CacheSpec lottoDraws,
            @Valid @NotNull CacheSpec latestDraw,
            @Valid @NotNull CacheSpec historyWinners
    ) {
        public record CacheSpec(
                @Min(1) long maximumSize,
                @NotNull Duration expireAfterWrite
        ) {}
    }
}
