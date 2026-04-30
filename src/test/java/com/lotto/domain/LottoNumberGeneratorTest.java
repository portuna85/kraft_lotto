package com.lotto.domain;

import com.lotto.TestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LottoNumberGeneratorTest {

    @Test
    void 생성된_번호는_유효성_규칙을_모두_만족한다() {
        var gen = new LottoNumberGenerator(TestFixtures.propertiesWithGenerator(1, 45, 6));
        for (int i = 0; i < 100; i++) {
            var ln = gen.generate();
            assertThat(ln.numbers()).hasSize(6).doesNotHaveDuplicates();
            assertThat(ln.numbers()).allMatch(n -> n >= 1 && n <= 45);
            assertThat(ln.numbers()).isSorted();
        }
    }

    @Test
    void 잘못된_범위로_초기화_시_예외() {
        assertThatThrownBy(() -> new LottoNumberGenerator(TestFixtures.propertiesWithGenerator(10, 5, 6)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pickSize_가_범위_초과면_예외() {
        assertThatThrownBy(() -> new LottoNumberGenerator(TestFixtures.propertiesWithGenerator(1, 5, 6)))
                .isInstanceOf(IllegalStateException.class);
    }
}

