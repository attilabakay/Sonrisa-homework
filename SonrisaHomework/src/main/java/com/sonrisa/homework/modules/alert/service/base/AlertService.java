package com.sonrisa.homework.modules.alert.service.base;

import com.sonrisa.homework.auth.RequestingUser;
import com.sonrisa.homework.modules.alert.dto.base.AlertDTO;
import com.sonrisa.homework.modules.alert.dto.request.AlertRequest;

import java.util.List;
import java.util.UUID;

public interface AlertService {

    AlertDTO create(AlertRequest request, RequestingUser requester);

    // User's "view active" list (workflow.md §4), scoped to the logged-in user
    // (RequestingUser.canAccess enforces the scoping — this used to be UI-only).
    List<AlertDTO> getActiveByUser(UUID userId, RequestingUser requester);

    // Admin visibility into which alerts (active or not) belong to which user (mvp final.md §5).
    // Already ROLE_ADMIN-gated at the security-filter level (SecurityConfig), so no requester
    // check needed here.
    List<AlertDTO> getAllByUser(UUID userId);

    AlertDTO getById(UUID id, RequestingUser requester);

    // Delete + recreate is the MVP's answer to editing — no update method.
    AlertDTO deactivate(UUID id, RequestingUser requester);

    void delete(UUID id, RequestingUser requester);
}
