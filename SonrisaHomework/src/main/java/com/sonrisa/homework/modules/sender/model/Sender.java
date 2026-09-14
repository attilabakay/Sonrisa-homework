package com.sonrisa.homework.modules.sender.model;

import com.sonrisa.homework.common.enums.SenderType;
import com.sonrisa.homework.modules.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "senders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sender {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Address, webhook URL, token, etc. — shape depends on `type`.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String config;
}
