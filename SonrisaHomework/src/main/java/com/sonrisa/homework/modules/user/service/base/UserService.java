package com.sonrisa.homework.modules.user.service.base;

import com.sonrisa.homework.modules.user.dto.base.UserDTO;
import com.sonrisa.homework.modules.user.dto.request.UserRegisterRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDTO register(UserRegisterRequest request);

    List<UserDTO> getAll();

    UserDTO getById(UUID id);

    UserDTO deactivate(UUID id);
}
