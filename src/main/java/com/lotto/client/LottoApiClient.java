package com.lotto.client;

import com.lotto.client.dto.LottoDrawResponse;

import java.util.Optional;

/**
 * 로또 회차 정보 조회 추상화 (DIP).
 * 외부 시스템 변경 시 본 인터페이스 구현만 추가하면 된다.
 */
public interface LottoApiClient {

    /**
     * 회차 번호로 추첨 결과를 조회한다.
     * @param drawNo 회차 번호
     * @return 성공 응답이면 값 존재, 실패 또는 미존재 시 empty
     */
    Optional<LottoDrawResponse> fetchDraw(int drawNo);
}
