package com.sonrisa.homework.modules.alert.service.base;

import com.sonrisa.homework.modules.alert.dto.base.AlertDTO;
import com.sonrisa.homework.modules.alert.dto.request.AlertRequest;

import java.util.List;
import java.util.UUID;

public interface AlertService {

    AlertDTO create(AlertRequest request);

    // User's "view active" list (workflow.md §4), scoped to the logged-in user.
    List<AlertDTO> getActiveByUser(UUID userId);

    AlertDTO getById(UUID id);

    // Delete + recreate is the MVP's answer to editing — no update method.
    AlertDTO deactivate(UUID id);

    void delete(UUID id);
}
