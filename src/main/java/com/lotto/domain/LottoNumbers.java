package com.lotto.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 6개의 정렬된 로또 번호를 표현하는 불변 값 객체.
 * 검증 규칙:
 *  - 정확히 6개
 *  - null 없음
 *  - 각 번호는 [1, 45] 범위
 *  - 중복 없음
 */
public record LottoNumbers(List<Integer> numbers) {

    public static final int SIZE = 6;
    public static final int MIN = 1;
    public static final int MAX = 45;

    /** 사용자 입력 문자열의 허용 구분자: 콤마, 세미콜론, 공백, 하이픈. */
    private static final String DELIMITERS = "[,;\\s\\-]+";

    public LottoNumbers {
        if (numbers == null || numbers.size() != SIZE) {
            throw new IllegalArgumentException("로또 번호는 정확히 " + SIZE + "개여야 합니다.");
        }
        // 단일 패스로 null/범위/중복을 모두 검사 (스트림 다중 순회 제거)
        Set<Integer> seen = new HashSet<>(SIZE);
        for (Integer n : numbers) {
            if (n == null) {
                throw new IllegalArgumentException("로또 번호에 null 이 포함될 수 없습니다.");
            }
            if (n < MIN || n > MAX) {
                throw new IllegalArgumentException("로또 번호는 " + MIN + " ~ " + MAX + " 범위여야 합니다: " + n);
            }
            if (!seen.add(n)) {
                throw new IllegalArgumentException("로또 번호는 중복될 수 없습니다: " + n);
            }
        }
        numbers = numbers.stream().sorted().toList();
    }

    /**
     * 사용자 입력 문자열을 도메인 객체로 파싱한다.
     * 허용 구분자: 콤마, 세미콜론, 공백, 하이픈. 예) "1, 2, 3, 4, 5, 6".
     *
     * @throws IllegalArgumentException 빈 문자열, 숫자 형식 오류, 또는 검증 실패 시
     */
    public static LottoNumbers parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("수동 번호가 비어 있습니다.");
        }
        try {
            List<Integer> nums = Arrays.stream(raw.split(DELIMITERS))
                    .filter(s -> !s.isBlank())
                    .map(Integer::parseInt)
                    .toList();
            return new LottoNumbers(nums);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("수동 번호 형식이 잘못되었습니다: " + raw);
        }
    }

    public String toFormattedString() {
        return numbers.stream()
                .map(n -> String.format("%2d", n))
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
