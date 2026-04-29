package com.lotto.service;

import com.lotto.config.LottoProperties;
import com.lotto.domain.LottoNumbers;
import com.lotto.domain.LottoTicket;
import com.lotto.domain.PickMode;
import com.lotto.domain.ReceiptNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class LottoTicketServiceTest {

    private LottoService lottoService;
    private ReceiptNumberGenerator receipt;
    private LottoTicketService service;

    private static LottoProperties props() {
        return new LottoProperties(
                new LottoProperties.Api("http://x", "m",
                        Duration.ofSeconds(1), Duration.ofSeconds(1), 50,
                        new LottoProperties.Api.Retry(3, Duration.ofMillis(200), 2.0, Duration.ofSeconds(2))),
                new LottoProperties.Draw(1100, 100),
                new LottoProperties.Generator(5, 50, 1, 45, 6),
                new LottoProperties.Ticket(1000, 365)
        );
    }

    @BeforeEach
    void setUp() {
        lottoService = Mockito.mock(LottoService.class);
        receipt = Mockito.mock(ReceiptNumberGenerator.class);
        when(receipt.generate()).thenReturn("00001 00002 00003 00004 00005 00006");
        // 자동 픽은 LottoService.generate(count, skipHistory) 단일 진입점에 위임됨
        when(lottoService.generate(anyInt(), anyBoolean())).thenAnswer(inv -> {
            int n = inv.getArgument(0, Integer.class);
            boolean skip = inv.getArgument(1, Boolean.class);
            List<LottoNumbers> picks = java.util.stream.IntStream.range(0, n)
                    .mapToObj(i -> new LottoNumbers(List.of(1, 2, 3, 4, 5, 6)))
                    .toList();
            return new LottoService.GenerationResult(skip ? 0 : 1175, skip ? 0 : 1175, picks);
        });
        service = new LottoTicketService(lottoService, receipt, props());
    }

    @Test
    void skipHistory_true_면_skipHistory_플래그가_LottoService_에_그대로_전달된다() {
        LottoTicket ticket = service.issue(3, 1, null, true);
        assertThat(ticket.games()).hasSize(3);
        assertThat(ticket.games().get(0).mode()).isEqualTo(PickMode.MANUAL);
        assertThat(ticket.games().get(1).mode()).isEqualTo(PickMode.AUTO);
        assertThat(ticket.totalPrice()).isEqualTo(3000);
        assertThat(ticket.round()).isEqualTo(0);
        Mockito.verify(lottoService).generate(3, true);
    }

    @Test
    void 수동_번호_입력시_앞쪽_슬롯에_배치되고_MANUAL_라벨_부여() {
        var manual = List.of(
                new LottoNumbers(List.of(11, 12, 13, 14, 15, 16)),
                new LottoNumbers(List.of(21, 22, 23, 24, 25, 26))
        );
        LottoTicket ticket = service.issue(5, null, manual, true);
        assertThat(ticket.games()).hasSize(5);
        assertThat(ticket.games().get(0).numbers().numbers()).containsExactly(11, 12, 13, 14, 15, 16);
        assertThat(ticket.games().get(1).numbers().numbers()).containsExactly(21, 22, 23, 24, 25, 26);
        assertThat(ticket.games().get(0).mode()).isEqualTo(PickMode.MANUAL);
        assertThat(ticket.games().get(1).mode()).isEqualTo(PickMode.MANUAL);
        assertThat(ticket.games().get(2).mode()).isEqualTo(PickMode.AUTO);
    }

    @Test
    void 게임수가_max_초과면_예외() {
        assertThatThrownBy(() -> service.issue(51, 0, null, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 수동_입력이_총게임수_초과면_예외() {
        var manual = List.of(
                new LottoNumbers(List.of(1, 2, 3, 4, 5, 6)),
                new LottoNumbers(List.of(7, 8, 9, 10, 11, 12))
        );
        assertThatThrownBy(() -> service.issue(1, 0, manual, true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

