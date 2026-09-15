package com.sonrisa.homework.modules.sender.service.base;

import com.sonrisa.homework.auth.RequestingUser;
import com.sonrisa.homework.modules.sender.dto.base.SenderDTO;
import com.sonrisa.homework.modules.sender.dto.request.SenderRequest;

import java.util.List;
import java.util.UUID;

// A Sender belongs to a user, same as an Alert — every method here is scoped to the
// requester's own resources unless they're admin (RequestingUser.canAccess).
public interface SenderService {

    SenderDTO create(SenderRequest request, RequestingUser requester);

    List<SenderDTO> getByUser(UUID userId, RequestingUser requester);

    SenderDTO getById(UUID id, RequestingUser requester);

    SenderDTO update(UUID id, SenderRequest request, RequestingUser requester);

    void delete(UUID id, RequestingUser requester);
}
