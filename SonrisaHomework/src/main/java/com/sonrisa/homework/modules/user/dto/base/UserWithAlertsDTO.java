package com.sonrisa.homework.modules.user.dto.base;

import com.sonrisa.homework.modules.alert.dto.base.AlertDTO;
import com.sonrisa.homework.modules.user.model.User;

import java.util.List;
import java.util.UUID;

// Admin "get all users" view, joined to their Alerts (workflow.md §1) -- only used by the
// admin list endpoint. Registration/deactivate/"who am I" stay on the plain UserDTO since
// those only ever concern the caller's own account and don't need the extra join.
public record UserWithAlertsDTO(
        UUID id,
        String email,
        boolean admin,
        boolean active,
        List<AlertDTO> alerts
) {
    public static UserWithAlertsDTO fromEntity(User entity, List<AlertDTO> alerts) {
        return new UserWithAlertsDTO(entity.getId(), entity.getEmail(), entity.isAdmin(), entity.isActive(), alerts);
    }
}
