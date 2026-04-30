package com.lotto.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManualNumberParserTest {

    @Test
    void null_입력은_빈_목록() {
        assertThat(ManualNumberParser.parse(null)).isEmpty();
    }

    @Test
    void 빈_목록은_빈_목록() {
        assertThat(ManualNumberParser.parse(List.of())).isEmpty();
    }

    @Test
    void 공백_및_null_엔트리는_건너뛴다() {
        List<String> raw = Arrays.asList("1,2,3,4,5,6", "  ", null, "7,8,9,10,11,12");
        var parsed = ManualNumberParser.parse(raw);
        assertThat(parsed).hasSize(2);
        assertThat(parsed.get(0).numbers()).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(parsed.get(1).numbers()).containsExactly(7, 8, 9, 10, 11, 12);
    }

    @Test
    void 잘못된_입력은_LottoNumbers_검증에_위임되어_예외() {
        assertThatThrownBy(() -> ManualNumberParser.parse(List.of("1,2,3")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
