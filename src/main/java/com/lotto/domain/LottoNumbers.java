package com.lotto.domain;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 6개의 정렬된 로또 번호를 표현하는 불변 값 객체.
 * 검증 규칙:
 *  - 정확히 6개
 *  - 중복 없음
 *  - 각 번호는 [1, 45] 범위
 */
public record LottoNumbers(List<Integer> numbers) {

    public static final int SIZE = 6;
    public static final int MIN = 1;
    public static final int MAX = 45;

    public LottoNumbers {
        if (numbers == null || numbers.size() != SIZE) {
            throw new IllegalArgumentException("로또 번호는 정확히 " + SIZE + "개여야 합니다.");
        }
        if (numbers.stream().distinct().count() != SIZE) {
            throw new IllegalArgumentException("로또 번호는 중복될 수 없습니다.");
        }
        if (numbers.stream().anyMatch(n -> n == null || n < MIN || n > MAX)) {
            throw new IllegalArgumentException("로또 번호는 " + MIN + " ~ " + MAX + " 범위여야 합니다.");
        }
        numbers = numbers.stream().sorted().toList();
    }

    public String toFormattedString() {
        return numbers.stream()
                .map(n -> String.format("%2d", n))
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
