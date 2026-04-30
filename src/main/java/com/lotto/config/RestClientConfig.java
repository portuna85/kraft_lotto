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

    /**
     * Caffeine 기반 캐시 매니저. 사이즈/TTL 은 {@link LottoProperties.Cache} 에서 외부 주입 받아
     * 코드 수정 없이 운영 튜닝 가능하도록 일원화했다.
     */
    @Bean
    public CacheManager cacheManager(LottoProperties properties) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        LottoProperties.Cache cache = properties.cache();
        manager.registerCustomCache("lottoDraws", buildCache(cache.lottoDraws()));
        manager.registerCustomCache("latestDraw", buildCache(cache.latestDraw()));
        manager.registerCustomCache("historyWinners", buildCache(cache.historyWinners()));
        return manager;
    }

    private static com.github.benmanes.caffeine.cache.Cache<Object, Object> buildCache(
            LottoProperties.Cache.CacheSpec spec) {
        return Caffeine.newBuilder()
                .maximumSize(spec.maximumSize())
                .expireAfterWrite(spec.expireAfterWrite())
                .recordStats()
                .build();
    }
}
