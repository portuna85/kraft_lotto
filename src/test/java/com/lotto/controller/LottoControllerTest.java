package com.lotto.controller;

import com.lotto.config.LottoProperties;
import com.lotto.domain.LottoNumbers;
import com.lotto.domain.LottoTicket;
import com.lotto.domain.PickMode;
import com.lotto.domain.TicketGame;
import com.lotto.service.LottoService;
import com.lotto.service.LottoTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc 단위 테스트.
 * Spring Boot 4 에서 슬라이스({@code @WebMvcTest})가 제거되어 표준 빌더로 가볍게 검증한다.
 *
 * <p>주의: 클래스 레벨 {@code @Validated} 메서드 검증 AOP 는 standaloneSetup 에서
 * 활성화되지 않으므로 @Min/@Max 위반 케이스는 본 슬라이스에서 다루지 않는다.</p>
 */
@ExtendWith(MockitoExtension.class)
class LottoControllerTest {

    @Mock LottoService lottoService;
    @Mock LottoTicketService lottoTicketService;

    private MockMvc mvc;

    private static LottoProperties props() {
        return new LottoProperties(
                new LottoProperties.Api("http://x", "m", Duration.ofSeconds(1), Duration.ofSeconds(1)),
                new LottoProperties.Draw(1100, 100),
                new LottoProperties.Generator(5, 50, 1, 45, 6),
                new LottoProperties.Ticket(1000, 365)
        );
    }

    @BeforeEach
    void setUp() {
        var controller = new LottoController(lottoService, lottoTicketService, props());
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void generate_정상_응답() throws Exception {
        when(lottoService.generateUnique(anyInt())).thenReturn(
                new LottoService.GenerationResult(1175, 1175,
                        List.of(new LottoNumbers(List.of(1, 2, 3, 4, 5, 6))))
        );
        mvc.perform(get("/api/lotto/generate?count=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestDraw").value(1175))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.generatedNumbers[0][0]").value(1));
    }


    @Test
    void generate_count_타입_오류시_400() throws Exception {
        mvc.perform(get("/api/lotto/generate?count=abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("파라미터 형식 오류"));
    }

    @Test
    void ticket_정상_응답() throws Exception {
        var games = List.of(
                new TicketGame("A", PickMode.AUTO, new LottoNumbers(List.of(1, 2, 3, 4, 5, 6)))
        );
        var ticket = new LottoTicket(0,
                LocalDateTime.of(2026, 4, 29, 14, 30, 0),
                LocalDate.of(2026, 5, 2),
                LocalDate.of(2027, 5, 2),
                "00001 00002 00003 00004 00005 00006",
                games, 1000, 1000);
        when(lottoTicketService.issue(anyInt(), any(), any(), anyBoolean())).thenReturn(ticket);

        mvc.perform(get("/api/lotto/ticket?games=1&skipHistory=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("로또6/45"))
                .andExpect(jsonPath("$.price.total").value(1000))
                .andExpect(jsonPath("$.games[0].label").value("A"));
    }
}

