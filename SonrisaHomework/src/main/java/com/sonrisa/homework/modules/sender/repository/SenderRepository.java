package com.sonrisa.homework.modules.sender.repository;

import com.sonrisa.homework.modules.sender.model.Sender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SenderRepository extends JpaRepository<Sender, UUID> {
    List<Sender> findByUserId(UUID userId);
}
