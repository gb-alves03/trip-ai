package com.trip_ai.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "price_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceAlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_id", nullable = false)
    private TravelPreferenceEntity preference;

    @Column(name = "price_found", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceFound;

    @Column(name = "price_avg_reference", precision = 10, scale = 2)
    private BigDecimal priceAvgReference;

    @Column(name = "drop_percentage", precision = 5, scale = 2)
    private BigDecimal dropPercentage;

    @Column(length = 150)
    private String airline;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "affiliate_url", columnDefinition = "TEXT")
    private String affiliateUrl;

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    @Column(name = "user_action", length = 20)
    private String userAction;
}
