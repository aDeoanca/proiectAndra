package com.pomanagement.domain.entity;

import com.pomanagement.domain.enums.HistoryAction;
import com.pomanagement.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "po_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder po;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private HistoryAction action;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Status fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status toStatus;

    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
