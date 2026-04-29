package com.lotto.domain;

import com.lotto.config.LottoProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 로또 번호 무작위 생성기 (SRP).
 * 외부 추첨 결과와 무관한 순수 도메인 컴포넌트.
 */
@Component
public class LottoNumberGenerator {

    private final LottoProperties properties;
    private final List<Integer> pool;

    public LottoNumberGenerator(LottoProperties properties) {
        this.properties = properties;
        var range = new ArrayList<Integer>(
                properties.generator().numberMax() - properties.generator().numberMin() + 1);
        for (int i = properties.generator().numberMin();
             i <= properties.generator().numberMax(); i++) {
            range.add(i);
        }
        this.pool = List.copyOf(range);
    }

    public LottoNumbers generate() {
        var copy = new ArrayList<>(pool);
        Collections.shuffle(copy, ThreadLocalRandom.current());
        var picked = new ArrayList<>(copy.subList(0, properties.generator().pickSize()));
        return new LottoNumbers(picked);
    }
}

