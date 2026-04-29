package com.lotto.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LottoNumbersTest {

    @Test
    void 정렬되어_저장된다() {
        var ln = new LottoNumbers(List.of(45, 1, 22, 7, 13, 30));
        assertThat(ln.numbers()).containsExactly(1, 7, 13, 22, 30, 45);
    }

    @Test
    void 개수가_6이_아니면_예외() {
        assertThatThrownBy(() -> new LottoNumbers(List.of(1, 2, 3, 4, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("6");
    }

    @Test
    void 중복이면_예외() {
        assertThatThrownBy(() -> new LottoNumbers(List.of(1, 2, 3, 4, 5, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("중복");
    }

    @Test
    void null_원소면_예외() {
        assertThatThrownBy(() -> new LottoNumbers(Arrays.asList(1, 2, 3, 4, 5, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void 범위_외_숫자면_예외() {
        assertThatThrownBy(() -> new LottoNumbers(List.of(0, 1, 2, 3, 4, 5)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LottoNumbers(List.of(1, 2, 3, 4, 5, 46)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toFormattedString_포맷() {
        var ln = new LottoNumbers(List.of(3, 11, 22, 28, 33, 41));
        assertThat(ln.toFormattedString()).isEqualTo("[ 3, 11, 22, 28, 33, 41]");
    }
}

