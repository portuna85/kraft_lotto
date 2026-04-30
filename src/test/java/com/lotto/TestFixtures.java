package com.lotto;

import com.lotto.config.LottoProperties;

import java.time.Duration;

/**
 * 테스트 공용 픽스처. 각 테스트가 동일한 {@link LottoProperties} 더미를 반복 생성하던
 * 보일러플레이트를 통합한다. 필요 시 빌더 스타일로 부분 오버라이드 가능.
 */
public final class TestFixtures {

    private TestFixtures() {
    }

    /** 표준 기본 프로퍼티 (대부분의 단위 테스트가 사용). */
    public static LottoProperties defaultProperties() {
        return propertiesWithDraw(1100, 100);
    }

    /** {@code Draw} 영역만 변경한 프로퍼티 (binary-search 테스트에서 사용). */
    public static LottoProperties propertiesWithDraw(int searchStart, int searchStep) {
        return propertiesWithGenerator(searchStart, searchStep, 1, 45, 6);
    }

    /** {@code Generator} 영역만 변경한 프로퍼티 (number-generator 테스트에서 사용). */
    public static LottoProperties propertiesWithGenerator(int numberMin, int numberMax, int pickSize) {
        return propertiesWithGenerator(1, 1, numberMin, numberMax, pickSize);
    }

    private static LottoProperties propertiesWithGenerator(
            int searchStart, int searchStep,
            int numberMin, int numberMax, int pickSize) {
        return new LottoProperties(
                new LottoProperties.Api("http://x", "m",
                        Duration.ofSeconds(1), Duration.ofSeconds(1), 50,
                        new LottoProperties.Api.Retry(3, Duration.ofMillis(200), 2.0, Duration.ofSeconds(2))),
                new LottoProperties.Draw(searchStart, searchStep),
                new LottoProperties.Generator(5, 50, numberMin, numberMax, pickSize, 1000),
                new LottoProperties.Ticket(1000, 365),
                new LottoProperties.Cache(
                        new LottoProperties.Cache.CacheSpec(2000, Duration.ofDays(7)),
                        new LottoProperties.Cache.CacheSpec(1, Duration.ofMinutes(30)),
                        new LottoProperties.Cache.CacheSpec(8, Duration.ofDays(7))
                )
        );
    }
}
