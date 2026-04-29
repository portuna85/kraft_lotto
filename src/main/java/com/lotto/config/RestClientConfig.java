package com.lotto.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableCaching
public class RestClientConfig {

    @Bean
    public RestClient lottoRestClient(LottoProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.api().connectTimeout())
                .version(HttpClient.Version.HTTP_2)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.api().readTimeout());

        return RestClient.builder()
                .baseUrl(properties.api().baseUrl())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        // 추첨 결과는 한번 확정되면 영구 불변 → 7일 TTL (재시작 빈도 고려)
        manager.registerCustomCache("lottoDraws",
                Caffeine.newBuilder()
                        .maximumSize(2000)
                        .expireAfterWrite(Duration.ofDays(7))
                        .recordStats()
                        .build());
        // 최신 회차는 매주 토요일 갱신 → 30분 TTL
        manager.registerCustomCache("latestDraw",
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(Duration.ofMinutes(30))
                        .recordStats()
                        .build());
        // 역대 당첨번호 Set 캐시 (latestDraw 키) → 회차 변경 시까지 유지
        manager.registerCustomCache("historyWinners",
                Caffeine.newBuilder()
                        .maximumSize(8)
                        .expireAfterWrite(Duration.ofDays(7))
                        .recordStats()
                        .build());
        return manager;
    }
}
