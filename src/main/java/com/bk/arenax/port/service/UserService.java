package com.bk.arenax.port.service;

import com.bk.arenax.dto.request.CreateUserRequest;
import com.bk.arenax.dto.request.UpdateUserRequest;
import com.bk.arenax.dto.response.UserResponse;
import com.bk.arenax.shared.pagination.BasePaginationRequest;
import com.bk.arenax.shared.pagination.BasePaginationResponse;
import java.util.List;

public interface UserService {
  List<UserResponse> getUsers();

  BasePaginationResponse<UserResponse> getUsers(BasePaginationRequest request);

  UserResponse getUser(Long id);

  UserResponse createUser(CreateUserRequest request);

  UserResponse updateUser(Long id, UpdateUserRequest request);

  void deleteUser(Long id);
}
