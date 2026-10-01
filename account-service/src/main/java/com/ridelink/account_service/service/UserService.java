package com.ridelink.account_service.service;

import com.ridelink.account_service.dto.UpdateProfileRequest;
import com.ridelink.account_service.dto.UserResponse;
import com.ridelink.account_service.entity.User;
import com.ridelink.account_service.exception.ApiException;
import com.ridelink.account_service.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(findUser(id));
    }

    @Transactional
    public UserResponse updateProfile(Long id, UpdateProfileRequest req) {
        User user = findUser(id);
        if (req.fullName() != null && !req.fullName().isBlank()) {
            user.setFullName(req.fullName());
        }
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateStatus(Long id, User.AccountStatus status) {
        User user = findUser(id);
        user.setStatus(status);
        return UserResponse.from(userRepository.save(user));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND", "User not found with id " + id));
    }
}