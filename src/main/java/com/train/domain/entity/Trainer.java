package com.train.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trainers", indexes = {
    @Index(name = "idx_trainers_user_id", columnList = "user_id"),
    @Index(name = "idx_trainers_is_verified", columnList = "is_verified"),
    @Index(name = "idx_trainers_rating", columnList = "rating")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String specialization;

    @Column(nullable = false, unique = true, length = 100)
    private String certificationNumber;

    @Column(columnDefinition = "TEXT")
    private String certificationFileUrl;

    @Column(nullable = false)
    private Integer yearsOfExperience;

    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(columnDefinition = "TEXT")
    private String bioProfessional;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column
    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
