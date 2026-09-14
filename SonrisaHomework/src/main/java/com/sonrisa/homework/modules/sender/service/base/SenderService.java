package com.sonrisa.homework.modules.sender.service.base;

import com.sonrisa.homework.modules.sender.dto.base.SenderDTO;
import com.sonrisa.homework.modules.sender.dto.request.SenderRequest;

import java.util.List;
import java.util.UUID;

public interface SenderService {

    SenderDTO create(SenderRequest request);

    List<SenderDTO> getByUser(UUID userId);

    SenderDTO getById(UUID id);

    SenderDTO update(UUID id, SenderRequest request);

    void delete(UUID id);
}
