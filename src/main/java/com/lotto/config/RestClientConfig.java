package com.lotto.config;

import org.springframework.cache.annotation.EnableCaching;
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
        Duration connect = properties.api().connectTimeout();
        Duration read = properties.api().readTimeout();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connect)
                .version(HttpClient.Version.HTTP_2)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(read);

        return RestClient.builder()
                .baseUrl(properties.api().baseUrl())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
    }
}
