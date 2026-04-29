package com.lotto.service;

import com.lotto.domain.LottoNumberGenerator;
import com.lotto.domain.LottoNumbers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 도메인 협력자들을 조립하는 애플리케이션 서비스 (Facade).
 * - 비즈니스 시나리오: "역대 당첨번호와 겹치지 않는 신규 번호 N개를 생성한다"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LottoService {

    private final LottoDrawFinder lottoDrawFinder;
    private final LottoHistoryFetcher lottoHistoryFetcher;
    private final LottoNumberGenerator lottoNumberGenerator;

    public GenerationResult generateUnique(int count) {
        int latestDraw = lottoDrawFinder.findLatestDraw();
        Set<LottoNumbers> historicalWinners = lottoHistoryFetcher.fetchAllWinners(latestDraw);
        List<LottoNumbers> generated = generateUnique(count, historicalWinners);
        return new GenerationResult(latestDraw, historicalWinners.size(), generated);
    }

    private List<LottoNumbers> generateUnique(int count, Set<LottoNumbers> historicalWinners) {
        Set<LottoNumbers> generated = new LinkedHashSet<>();
        int attempts = 0;
        int safetyLimit = count * 1000;
        while (generated.size() < count) {
            if (++attempts > safetyLimit) {
                throw new IllegalStateException("고유 번호 생성 한도를 초과했습니다.");
            }
            LottoNumbers candidate = lottoNumberGenerator.generate();
            if (!historicalWinners.contains(candidate)) {
                generated.add(candidate);
            }
        }
        if (attempts > count) {
            log.debug("번호 생성 시도 횟수: {} (목표: {})", attempts, count);
        }
        return List.copyOf(generated);
    }

    public record GenerationResult(
            int latestDraw,
            int historicalWinnerCount,
            List<LottoNumbers> generated
    ) {}
}
