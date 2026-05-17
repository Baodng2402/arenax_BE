package com.bk.arenax.adapter.service;

import com.bk.arenax.dto.request.CreateUserRequest;
import com.bk.arenax.dto.request.UpdateUserRequest;
import com.bk.arenax.domain.user.User;
import com.bk.arenax.dto.response.UserResponse;
import com.bk.arenax.port.repository.UserRepository;
import com.bk.arenax.port.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserResponse getUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return toUserResponse(user.get());
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getGender()
        );
        }

    @Override
    public User createUser(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.email())){
            throw new RuntimeException("Email already exists: " + request.email());
        } else {
            User user = new User();
            user.setName(request.name());
            user.setEmail(request.email());
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setGender(request.gender());
            return userRepository.save(user);
        }

    }

    @Override
    public User updateUser(Long id, UpdateUserRequest request) {
        User existingUser = userRepository.findById(id).orElseThrow(()-> new RuntimeException("User not found with id: " + id));
            existingUser.setName(request.name());
            existingUser.setEmail(request.email());
            return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        if(userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }
}
