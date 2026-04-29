package com.lotto.controller.dto;

import com.lotto.domain.LottoTicket;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public record TicketResponse(
        String title,
        int round,
        String issuedAt,
        String drawDate,
        String claimDeadline,
        String receiptNumber,
        List<GameLine> games,
        Price price
) {

    private static final Locale DISPLAY_LOCALE = Locale.KOREAN;
    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd (E) HH:mm:ss", DISPLAY_LOCALE);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public static TicketResponse from(LottoTicket t) {
        var games = t.games().stream()
                .map(g -> new GameLine(
                        g.label(),
                        g.mode().name(),
                        g.mode().label(),
                        g.numbers().numbers()
                ))
                .toList();
        return new TicketResponse(
                "로또6/45",
                t.round(),
                t.issuedAt().format(DATETIME_FMT),
                t.drawDate().format(DATE_FMT),
                t.claimDeadline().format(DATE_FMT),
                t.receiptNumber(),
                games,
                new Price(t.pricePerGame(), t.totalPrice(), "원")
        );
    }

    public record GameLine(
            String label,
            String mode,
            String modeLabel,
            List<Integer> numbers
    ) {}

    public record Price(int unit, int total, String currency) {}
}

