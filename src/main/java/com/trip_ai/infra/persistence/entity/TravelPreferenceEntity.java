package com.trip_ai.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "travel_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "origin_iata", nullable = false, length = 3)
    private String originIata;

    @Column(name = "destination_iata", nullable = false, length = 3)
    private String destinationIata;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "flexible_dates", nullable = false)
    @Builder.Default
    private boolean flexibleDates = true;

    @Column(name = "max_budget", precision = 10, scale = 2)
    private BigDecimal maxBudget;

    @Column(name = "direct_only", nullable = false)
    @Builder.Default
    private boolean directOnly = false;

    @Column(nullable = false)
    @Builder.Default
    private int passengers = 1;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
