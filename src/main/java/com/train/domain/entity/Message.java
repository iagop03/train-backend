package com.train.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_messages_receiver_id", columnList = "receiver_id"),
    @Index(name = "idx_messages_sender_id", columnList = "sender_id"),
    @Index(name = "idx_messages_status", columnList = "status"),
    @Index(name = "idx_messages_created_at", columnList = "created_at"),
    @Index(name = "idx_messages_receiver_status", columnList = "receiver_id, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageStatus status = MessageStatus.UNREAD;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeletedBySender = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeletedByReceiver = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    public enum MessageStatus {
        UNREAD, READ, ARCHIVED
    }
}
