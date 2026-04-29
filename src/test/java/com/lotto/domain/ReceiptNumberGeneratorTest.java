package com.lotto.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptNumberGeneratorTest {

    @Test
    void 다섯자리_여섯그룹_공백_구분() {
        var gen = new ReceiptNumberGenerator();
        String s = gen.generate();
        assertThat(s).matches("\\d{5} \\d{5} \\d{5} \\d{5} \\d{5} \\d{5}");
    }
}

