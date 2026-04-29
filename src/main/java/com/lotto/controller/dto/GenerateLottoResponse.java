package com.lotto.controller.dto;

import java.util.List;

public record GenerateLottoResponse(
        int latestDraw,
        int historicalWinnerCount,
        int count,
        List<List<Integer>> generatedNumbers,
        List<String> formattedNumbers
) {}

