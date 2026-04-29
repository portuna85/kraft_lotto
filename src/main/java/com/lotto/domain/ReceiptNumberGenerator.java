package com.lotto.domain;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 동행복권 영수증 번호 형식(5자리 6그룹) 생성기.
 * 예) "68765 57128 51424 59983 79420 00166"
 */
@Component
public class ReceiptNumberGenerator {

    private static final int GROUP_COUNT = 6;
    private static final int GROUP_SIZE = 5;
    private static final int GROUP_BOUND = 100_000; // 5자리
    private static final String GROUP_FORMAT = "%0" + GROUP_SIZE + "d";

    public String generate() {
        var random = ThreadLocalRandom.current();
        return IntStream.range(0, GROUP_COUNT)
                .mapToObj(i -> String.format(GROUP_FORMAT, random.nextInt(GROUP_BOUND)))
                .collect(Collectors.joining(" "));
    }
}

