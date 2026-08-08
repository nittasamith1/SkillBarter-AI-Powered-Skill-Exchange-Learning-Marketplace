package com.skillbarter.user.service;

import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.user.dto.UpdateProfileRequest;
import com.skillbarter.user.dto.UserResponse;
import com.skillbarter.user.entity.User;
import com.skillbarter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (StringUtils.hasText(request.firstName())) {
            user.setFirstName(request.firstName());
        }
        if (StringUtils.hasText(request.lastName())) {
            user.setLastName(request.lastName());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.location() != null) {
            user.setLocation(request.location());
        }
        if (StringUtils.hasText(request.preferredLanguage())) {
            user.setPreferredLanguage(request.preferredLanguage());
        }

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }
}
