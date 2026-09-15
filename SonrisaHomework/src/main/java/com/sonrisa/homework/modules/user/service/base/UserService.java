package com.sonrisa.homework.modules.user.service.base;

import com.sonrisa.homework.modules.user.dto.base.UserDTO;
import com.sonrisa.homework.modules.user.dto.request.UserRegisterRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDTO register(UserRegisterRequest request);

    List<UserDTO> getAll();

    UserDTO getById(UUID id);

    // Backs GET /api/users/me — resolves the HTTP Basic principal's email to a UserDTO.
    UserDTO getByEmail(String email);

    UserDTO deactivate(UUID id);
}
