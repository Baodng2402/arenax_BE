package com.bk.arenax.port.service;

import com.bk.arenax.dto.request.CreateUserRequest;
import com.bk.arenax.dto.request.UpdateUserRequest;
import com.bk.arenax.domain.user.User;
import com.bk.arenax.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<User> getUsers();

    UserResponse getUser(Long id);

    User createUser(CreateUserRequest request);

    User updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}
