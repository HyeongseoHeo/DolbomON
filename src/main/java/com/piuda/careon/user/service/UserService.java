package com.piuda.careon.user.service;

import com.piuda.careon.institution.entity.Institution;
import com.piuda.careon.user.dto.ChangePasswordRequest;
import com.piuda.careon.user.dto.CreateUserRequest;
import com.piuda.careon.user.dto.ResetPasswordResponse;
import com.piuda.careon.user.dto.UserResponse;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.entity.UserRole;
import com.piuda.careon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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
                        passwordEncoder.encode("1234")
                )
                .role(request.role())
                .agreedTerms(false)
                .isActive(true)
                .mustChangePassword(true)
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

    /**
     * 본인 비밀번호 변경
     * 현재 비밀번호를 확인한 뒤
     * 새 비밀번호로 변경한다.
     */
    @Transactional
    public void changeMyPassword(
            String userEmail,
            ChangePasswordRequest request
    ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "현재 비밀번호가 일치하지 않습니다."
            );
        }

        // 현재 비밀번호와 새 비밀번호가 같은지 확인
        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "새 비밀번호는 현재 비밀번호와 달라야 합니다."
            );
        }

        String encodedNewPassword =
                passwordEncoder.encode(
                        request.newPassword()
                );

        /*
         * User.changePassword()에서
         * mustChangePassword도 false로 변경됨.
         */
        user.changePassword(encodedNewPassword);
    }

    /**
     * 관리자 - 사용자 비밀번호 초기화
     * 랜덤 임시 비밀번호를 생성하고
     * DB에는 BCrypt 해시만 저장
     */
    @Transactional
    public ResetPasswordResponse resetUserPassword(
            String adminEmail,
            UUID userId
    ) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "관리자 정보를 찾을 수 없습니다."
                        )
                );

        // ADMIN만 초기화 가능
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException(
                    "비밀번호를 초기화할 권한이 없습니다."
            );
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        // 다른 기관 사용자 초기화 방지
        if (admin.getInstitution() == null ||
                targetUser.getInstitution() == null ||
                !admin.getInstitution().getId()
                        .equals(targetUser.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "다른 기관 사용자의 비밀번호는 초기화할 수 없습니다."
            );
        }

        String temporaryPassword =
                generateTemporaryPassword();

        String encodedTemporaryPassword =
                passwordEncoder.encode(
                        temporaryPassword
                );

        /*
         * User.resetPassword()에서
         * mustChangePassword = true 처리.
         */
        targetUser.resetPassword(
                encodedTemporaryPassword
        );

        return new ResetPasswordResponse(
                temporaryPassword,
                true
        );
    }

    private String generateTemporaryPassword() {

        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghijkmnopqrstuvwxyz";
        String digits = "23456789";
        String special = "!@#$%";

        String all =
                upper + lower + digits + special;

        SecureRandom random =
                new SecureRandom();

        StringBuilder password =
                new StringBuilder();

        // 각 종류 최소 1개 보장
        password.append(
                upper.charAt(random.nextInt(upper.length()))
        );

        password.append(
                lower.charAt(random.nextInt(lower.length()))
        );

        password.append(
                digits.charAt(random.nextInt(digits.length()))
        );

        password.append(
                special.charAt(random.nextInt(special.length()))
        );

        // 총 12자리
        while (password.length() < 12) {
            password.append(
                    all.charAt(random.nextInt(all.length()))
            );
        }

        // 앞 4글자가 규칙적으로 보이지 않도록 섞기
        char[] chars =
                password.toString().toCharArray();

        for (int i = chars.length - 1; i > 0; i--) {

            int j = random.nextInt(i + 1);

            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
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
