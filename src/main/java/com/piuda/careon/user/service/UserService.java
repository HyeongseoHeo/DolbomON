package com.piuda.careon.user.service;

import com.piuda.careon.institution.entity.Institution;
import com.piuda.careon.user.dto.CreateUserRequest;
import com.piuda.careon.user.dto.UserResponse;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.entity.UserRole;
import com.piuda.careon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 관리자 - 기관 사용자 생성
     */
    @Transactional
    public UserResponse createUser(
            String adminEmail,
            CreateUserRequest request
    ) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "관리자 정보를 찾을 수 없습니다."
                        )
                );

        // ADMIN만 사용자 추가 가능
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException(
                    "사용자를 추가할 권한이 없습니다."
            );
        }

        // 기관 웹에서는 관리자 추가 금지
        if (request.role() == UserRole.ADMIN) {
            throw new IllegalArgumentException(
                    "관리자 계정은 사용자 관리 화면에서 생성할 수 없습니다."
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다."
            );
        }

        Institution institution = admin.getInstitution();

        User user = User.builder()
                .institution(institution)
                .name(request.name())
                .phone(request.phone())
                .email(request.email())
                .passwordHash(
                        passwordEncoder.encode(request.password())
                )
                .role(request.role())
                .agreedTerms(false)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    /**
     * 같은 기관 사용자 목록
     */
    public List<UserResponse> getInstitutionUsers(
            String userEmail
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        Institution institution =
                currentUser.getInstitution();

        return userRepository
                .findByInstitutionOrderByCreatedAtDesc(institution)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 사용자 상세 조회
     */
    public UserResponse getUser(
            String userEmail,
            UUID userId
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        // 다른 기관 사용자 접근 방지
        if (!currentUser.getInstitution().getId()
                .equals(targetUser.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "해당 사용자를 조회할 권한이 없습니다."
            );
        }

        return toResponse(targetUser);
    }

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getIsActive(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
