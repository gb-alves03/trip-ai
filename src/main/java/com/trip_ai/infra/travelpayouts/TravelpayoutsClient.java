package com.trip_ai.infra.travelpayouts;

import com.fasterxml.jackson.databind.JsonNode;
import com.trip_ai.domain.model.FlightOffer;
import com.trip_ai.domain.port.out.FlightSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Adapter para a Travelpayouts Data API (Aviasales).
 * Docs: https://developers.travelpayouts.com/docs/aviasales-data-api
 * Free para afiliados — token obtido em https://travelpayouts.com
 */
@Component
@Slf4j
public class TravelpayoutsClient implements FlightSearchPort {

    private final WebClient webClient;

    @Value("${tripai.travelpayouts.token}")
    private String token;

    @Value("${tripai.travelpayouts.marker:}")
    private String marker;

    public TravelpayoutsClient(@Qualifier("travelpayoutsWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<FlightOffer> search(String origin, String destination,
                                    LocalDate dateFrom, LocalDate dateTo, int adults) {
        try {
            LocalDate departure = (dateFrom != null && !dateFrom.isBefore(LocalDate.now()))
                ? dateFrom
                : LocalDate.now().plusMonths(1).withDayOfMonth(1);

            JsonNode response = webClient.get()
                .uri(uri -> {
                    var b = uri.path("/aviasales/v3/prices_for_dates")
                        .queryParam("origin", origin)
                        .queryParam("destination", destination)
                        .queryParam("departure_at", departure.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .queryParam("currency", "BRL")
                        .queryParam("sorting", "price")
                        .queryParam("limit", 5)
                        .queryParam("token", token);

                    if (dateTo != null && !dateTo.isBefore(departure)) {
                        b.queryParam("return_at", dateTo.format(DateTimeFormatter.ISO_LOCAL_DATE));
                    } else {
                        b.queryParam("one_way", true);
                    }
                    return b.build();
                })
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            return parseOffers(response);

        } catch (Exception e) {
            log.error("Erro ao buscar voos {} → {}: {}", origin, destination, e.getMessage());
            return List.of();
        }
    }

    private List<FlightOffer> parseOffers(JsonNode response) {
        if (response == null || !response.path("success").asBoolean(false)) return List.of();

        JsonNode data = response.path("data");
        if (!data.isArray() || data.isEmpty()) return List.of();

        List<FlightOffer> offers = new ArrayList<>();
        for (JsonNode item : data) {
            try {
                BigDecimal price = BigDecimal.valueOf(item.path("price").asDouble());
                String airline   = item.path("airline").asText(null);
                int stops        = item.path("transfers").asInt(0);
                LocalDate dep    = LocalDate.parse(item.path("departure_at").asText().substring(0, 10));
                String url       = buildAffiliateUrl(item.path("link").asText(null));

                offers.add(FlightOffer.builder()
                    .price(price)
                    .airline(airline)
                    .stops(stops)
                    .departureDate(dep)
                    .affiliateUrl(url)
                    .build());

            } catch (Exception e) {
                log.warn("Erro ao parsear oferta Travelpayouts: {}", e.getMessage());
            }
        }

        offers.sort(Comparator.comparing(FlightOffer::getPrice));
        return offers;
    }

    private String buildAffiliateUrl(String link) {
        if (link == null || link.isBlank()) return null;
        String base = "https://www.aviasales.com" + link;
        if (marker == null || marker.isBlank()) return base;
        return base + (link.contains("?") ? "&" : "?") + "marker=" + marker;
    }
}
