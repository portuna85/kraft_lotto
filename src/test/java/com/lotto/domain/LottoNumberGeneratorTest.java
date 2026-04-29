package com.lotto.domain;

import com.lotto.config.LottoProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LottoNumberGeneratorTest {

    private static LottoProperties props(int min, int max, int pick) {
        return new LottoProperties(
                new LottoProperties.Api("http://x", "m",
                        Duration.ofSeconds(1), Duration.ofSeconds(1), 50,
                        new LottoProperties.Api.Retry(3, Duration.ofMillis(200), 2.0, Duration.ofSeconds(2))),
                new LottoProperties.Draw(1, 1),
                new LottoProperties.Generator(5, 50, min, max, pick),
                new LottoProperties.Ticket(1000, 365)
        );
    }

    @Test
    void 생성된_번호는_유효성_규칙을_모두_만족한다() {
        var gen = new LottoNumberGenerator(props(1, 45, 6));
        for (int i = 0; i < 100; i++) {
            var ln = gen.generate();
            assertThat(ln.numbers()).hasSize(6).doesNotHaveDuplicates();
            assertThat(ln.numbers()).allMatch(n -> n >= 1 && n <= 45);
            assertThat(ln.numbers()).isSorted();
        }
    }

    @Test
    void 잘못된_범위로_초기화_시_예외() {
        assertThatThrownBy(() -> new LottoNumberGenerator(props(10, 5, 6)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pickSize_가_범위_초과면_예외() {
        assertThatThrownBy(() -> new LottoNumberGenerator(props(1, 5, 6)))
                .isInstanceOf(IllegalStateException.class);
    }
}

