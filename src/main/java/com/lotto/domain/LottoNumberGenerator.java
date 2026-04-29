package com.lotto.domain;

import com.lotto.config.LottoProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 로또 번호 무작위 생성기 (SRP).
 * 외부 추첨 결과와 무관한 순수 도메인 컴포넌트.
 *
 * <p>구현 메모: 매 호출마다 풀 전체를 셔플하는 대신 부분 Fisher–Yates 셔플을
 * 사용하여 앞 {@code pickSize} 슬롯만 swap 한다. 호출당 swap 횟수가
 * {@code pool.size()}에서 {@code pickSize}로 감소한다.</p>
 */
@Component
public class LottoNumberGenerator {

    private final int pickSize;
    private final List<Integer> pool;

    public LottoNumberGenerator(LottoProperties properties) {
        int min = properties.generator().numberMin();
        int max = properties.generator().numberMax();
        this.pickSize = properties.generator().pickSize();
        if (max < min) {
            throw new IllegalStateException("numberMax(" + max + ") 는 numberMin(" + min + ") 이상이어야 합니다.");
        }
        if (pickSize > max - min + 1) {
            throw new IllegalStateException(
                    "pickSize(" + pickSize + ") 가 번호 범위(" + min + "~" + max + ")를 초과합니다.");
        }
        var range = new ArrayList<Integer>(max - min + 1);
        for (int i = min; i <= max; i++) {
            range.add(i);
        }
        this.pool = List.copyOf(range);
    }

    public LottoNumbers generate() {
        // 부분 Fisher–Yates: 앞 pickSize 슬롯만 무작위로 채운다 (O(pickSize) swap).
        var random = ThreadLocalRandom.current();
        var working = new ArrayList<>(pool);
        int last = working.size() - 1;
        for (int i = 0; i < pickSize; i++) {
            int j = i + random.nextInt(last - i + 1);
            if (j != i) {
                Integer tmp = working.get(i);
                working.set(i, working.get(j));
                working.set(j, tmp);
            }
        }
        return new LottoNumbers(new ArrayList<>(working.subList(0, pickSize)));
    }
}

