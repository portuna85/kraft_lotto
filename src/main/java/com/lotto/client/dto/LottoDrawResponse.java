package com.lotto.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 동행복권 API 응답 DTO.
 * 회차가 존재하지 않을 경우 returnValue="fail" 만 내려옴.
 */
public record LottoDrawResponse(
        @JsonProperty("returnValue") String returnValue,
        @JsonProperty("drwNo") Integer drwNo,
        @JsonProperty("drwNoDate") String drwNoDate,
        @JsonProperty("drwtNo1") Integer drwtNo1,
        @JsonProperty("drwtNo2") Integer drwtNo2,
        @JsonProperty("drwtNo3") Integer drwtNo3,
        @JsonProperty("drwtNo4") Integer drwtNo4,
        @JsonProperty("drwtNo5") Integer drwtNo5,
        @JsonProperty("drwtNo6") Integer drwtNo6,
        @JsonProperty("bnusNo") Integer bnusNo
) {
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(returnValue);
    }

    public List<Integer> winningNumbers() {
        if (!isSuccess()) {
            throw new IllegalStateException("실패 응답에서 당첨 번호를 조회할 수 없습니다. 회차: " + drwNo);
        }
        return List.of(drwtNo1, drwtNo2, drwtNo3, drwtNo4, drwtNo5, drwtNo6);
    }
}

