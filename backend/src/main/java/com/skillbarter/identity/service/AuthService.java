package com.skillbarter.identity.service;

import com.skillbarter.common.exception.AuthenticationException;
import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.JwtService;
import com.skillbarter.identity.dto.AuthResponse;
import com.skillbarter.identity.dto.LoginRequest;
import com.skillbarter.identity.dto.RefreshTokenRequest;
import com.skillbarter.identity.dto.RegisterRequest;
import com.skillbarter.tenant.entity.Tenant;
import com.skillbarter.tenant.repository.TenantRepository;
import com.skillbarter.user.dto.UserResponse;
import com.skillbarter.user.entity.Role;
import com.skillbarter.user.entity.User;
import com.skillbarter.user.repository.RoleRepository;
import com.skillbarter.user.repository.UserRepository;
import com.skillbarter.user.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                       TenantRepository tenantRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("DUPLICATE_EMAIL", "User with email '" + request.email() + "' already exists");
        }

        Tenant tenant = tenantRepository.findBySlug(request.tenantSlug())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", request.tenantSlug()));

        if (tenant.getStatus() != Tenant.TenantStatus.ACTIVE) {
            throw new BusinessException("TENANT_INACTIVE", "Tenant account is not active");
        }

        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new IllegalStateException("Default role 'STUDENT' not found in database"));

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setTenant(tenant);
        user.setRoles(Set.of(studentRole));
        user.setStatus(User.UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        auditService.log(tenant.getId(), savedUser.getId(), "REGISTER", "User", savedUser.getId().toString(), ipAddress);

        log.info("Registered user id={} email={} tenantId={}", savedUser.getId(), savedUser.getEmail(), tenant.getId());
        return UserResponse.from(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            auditService.log(user.getTenant().getId(), user.getId(), "LOGIN_FAILED", ipAddress);
            throw new AuthenticationException("Invalid email or password");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new AuthenticationException("Account is " + user.getStatus().name().toLowerCase());
        }

        List<String> roleNames = user.getRoles().stream().map(Role::getName).toList();
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roleNames, user.getTenant().getId());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        auditService.log(user.getTenant().getId(), user.getId(), "LOGIN_SUCCESS", ipAddress);

        return AuthResponse.of(accessToken, refreshToken, jwtService.getAccessExpirationMs(), UserResponse.from(user));
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress) {
        User user = refreshTokenService.verifyAndRotateToken(request.refreshToken());

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new AuthenticationException("Account is " + user.getStatus().name().toLowerCase());
        }

        List<String> roleNames = user.getRoles().stream().map(Role::getName).toList();
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roleNames, user.getTenant().getId());
        String newRefreshToken = refreshTokenService.createRefreshToken(user);

        auditService.log(user.getTenant().getId(), user.getId(), "TOKEN_REFRESH", ipAddress);

        return AuthResponse.of(newAccessToken, newRefreshToken, jwtService.getAccessExpirationMs(), UserResponse.from(user));
    }

    @Transactional
    public void logout(String refreshToken, String userIdStr, String ipAddress) {
        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }
        if (userIdStr != null) {
            try {
                auditService.log(null, java.util.UUID.fromString(userIdStr), "LOGOUT", ipAddress);
            } catch (Exception ignored) {
            }
        }
    }
}
