package com.trip_ai.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "price_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 3)
    private String origin;

    @Column(nullable = false, length = 3)
    private String destination;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 150)
    private String airline;

    @Column(nullable = false)
    @Builder.Default
    private int stops = 0;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String source = "amadeus";

    @Column(name = "affiliate_url", columnDefinition = "TEXT")
    private String affiliateUrl;

    @CreationTimestamp
    @Column(name = "captured_at", nullable = false, updatable = false)
    private Instant capturedAt;
}
