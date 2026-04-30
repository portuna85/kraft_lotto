package com.lotto.service;

import com.lotto.config.LottoProperties;
import com.lotto.domain.LottoNumberGenerator;
import com.lotto.domain.LottoNumbers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 도메인 협력자들을 조립하는 애플리케이션 서비스 (Facade).
 * - 비즈니스 시나리오: "역대 당첨번호와 겹치지 않는 신규 번호 N개를 생성한다"
 * - {@code skipHistory=true} 모드도 지원하여 동일 진입점으로 빠른 자동 픽을 제공한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LottoService {

    private final LottoDrawFinder lottoDrawFinder;
    private final LottoHistoryCache lottoHistoryCache;
    private final LottoNumberGenerator lottoNumberGenerator;
    private final LottoProperties properties;

    /**
     * 역대 당첨번호와 겹치지 않는 고유 번호 {@code count}개를 생성한다.
     * 외부 API 호출이 발생할 수 있다.
     */
    public GenerationResult generateUnique(int count) {
        return generate(count, false);
    }

    /**
     * 번호 생성 단일 진입점. {@code skipHistory=true} 면 외부 API/캐시를 건드리지 않고
     * 단순 무작위 자동 픽만 수행한다.
     *
     * @param count        생성할 조합 수
     * @param skipHistory  true 면 역대 당첨번호 조회를 건너뛴다
     */
    public GenerationResult generate(int count, boolean skipHistory) {
        if (count < 1) {
            throw new IllegalArgumentException("생성 개수는 1 이상이어야 합니다.");
        }
        if (skipHistory) {
            List<LottoNumbers> generated = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                generated.add(lottoNumberGenerator.generate());
            }
            return new GenerationResult(0, 0, List.copyOf(generated));
        }
        int latestDraw = lottoDrawFinder.findLatestDraw();
        Set<LottoNumbers> historicalWinners = lottoHistoryCache.getOrFetch(latestDraw);
        List<LottoNumbers> generated = generateAvoidingHistory(count, historicalWinners);
        return new GenerationResult(latestDraw, historicalWinners.size(), generated);
    }

    /**
     * 역대 당첨번호와 겹치지 않는 조합을 {@code count}개가 모일 때까지 시도한다.
     * 안전 한도(시도 횟수 = {@code count * safetyMultiplier})는 설정으로 외부화되어 있다.
     */
    private List<LottoNumbers> generateAvoidingHistory(int count, Set<LottoNumbers> historicalWinners) {
        Set<LottoNumbers> generated = new LinkedHashSet<>();
        int attempts = 0;
        int safetyLimit = count * properties.generator().safetyMultiplier();
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
