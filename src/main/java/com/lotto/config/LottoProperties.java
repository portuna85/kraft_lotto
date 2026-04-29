package com.lotto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "lotto")
public record LottoProperties(
        Api api,
        Draw draw,
        Generator generator,
        Ticket ticket
) {
    public record Api(
            String baseUrl,
            String method,
            Duration connectTimeout,
            Duration readTimeout
    ) {}

    public record Draw(
            int searchStart,
            int searchStep
    ) {}

    public record Generator(
            int defaultCount,
            int maxCount,
            int numberMin,
            int numberMax,
            int pickSize
    ) {}

    public record Ticket(
            int pricePerGame,
            int claimValidityDays
    ) {}
}
