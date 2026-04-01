package com.technoboost.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "email_verification_tokens", indexes = {
        @Index(name = "idx_email_verification_token", columnList = "token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken extends BaseEntity {

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private boolean used = false;

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}
