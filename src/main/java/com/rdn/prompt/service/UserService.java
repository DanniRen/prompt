package com.rdn.prompt.service;

import com.rdn.prompt.entity.User;
import com.rdn.prompt.entity.dto.RegistryDTO;
import com.rdn.prompt.util.ApiBaseResponse;

public interface UserService {
    User getById(String UserId);

    User getByName(String UserName);

    ApiBaseResponse login(RegistryDTO userDTO);

    ApiBaseResponse register(RegistryDTO userDTO);

    ApiBaseResponse deleteByUserId(String userId);


}
