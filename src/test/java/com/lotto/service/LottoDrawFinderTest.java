package com.lotto.service;

import com.lotto.client.LottoApiClient;
import com.lotto.client.dto.LottoDrawResponse;
import com.lotto.config.LottoProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LottoDrawFinderTest {

    private static LottoProperties props(int searchStart, int searchStep) {
        return new LottoProperties(
                new LottoProperties.Api("http://x", "m",
                        Duration.ofSeconds(1), Duration.ofSeconds(1), 50,
                        new LottoProperties.Api.Retry(3, Duration.ofMillis(200), 2.0, Duration.ofSeconds(2))),
                new LottoProperties.Draw(searchStart, searchStep),
                new LottoProperties.Generator(5, 50, 1, 45, 6),
                new LottoProperties.Ticket(1000, 365)
        );
    }

    private static LottoDrawResponse ok(int no) {
        return new LottoDrawResponse("success", no, "2025-01-01", 1, 2, 3, 4, 5, 6, 7);
    }

    @Test
    void 단순_경계_탐색() {
        // 마지막 유효 회차를 1175 로 가정
        LottoApiClient client = mock(LottoApiClient.class);
        int latest = 1175;
        when(client.fetchDraw(org.mockito.ArgumentMatchers.anyInt())).thenAnswer(inv -> {
            int n = inv.getArgument(0, Integer.class);
            return n <= latest ? Optional.of(ok(n)) : Optional.empty();
        });

        var finder = new LottoDrawFinder(client, props(1100, 100));
        int result = finder.findLatestDraw();
        assertThat(result).isEqualTo(latest);
    }

    @Test
    void 시작값이_이미_미존재면_시작값을_반환_또는_근접값() {
        LottoApiClient client = mock(LottoApiClient.class);
        // 모든 회차가 미존재 → 알고리즘 가정상 searchStart 가 유효해야 하지만,
        // 그렇지 않을 때도 NPE 없이 어떤 정수를 반환하는지 검증.
        when(client.fetchDraw(org.mockito.ArgumentMatchers.anyInt())).thenReturn(Optional.empty());

        var finder = new LottoDrawFinder(client, props(1100, 100));
        // 호출 자체로 예외가 나지 않으면 통과 (반환값은 구현 가정에 따라 달라질 수 있음)
        int result = finder.findLatestDraw();
        assertThat(result).isLessThanOrEqualTo(1200);
        verify(client, times(1)).fetchDraw(eq(1200));
    }
}

