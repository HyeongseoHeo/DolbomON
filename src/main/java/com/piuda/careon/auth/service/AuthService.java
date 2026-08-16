package com.piuda.careon.auth.service;

import com.piuda.careon.auth.jwt.JwtTokenProvider;
import com.piuda.careon.auth.dto.CheckInstitutionRequest;
import com.piuda.careon.auth.dto.CheckInstitutionResponse;
import com.piuda.careon.auth.dto.LoginRequest;
import com.piuda.careon.auth.dto.LoginResponse;
import com.piuda.careon.auth.dto.SignupRequest;
import com.piuda.careon.institution.entity.Institution;
import com.piuda.careon.institution.repository.InstitutionRepository;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.repository.UserRepository;
import com.piuda.careon.auth.dto.MeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public CheckInstitutionResponse checkInstitution(CheckInstitutionRequest request) {
        Institution institution = institutionRepository.findByCode(request.institutionCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 기관 코드입니다."));

        return new CheckInstitutionResponse(
                institution.getId(),
                institution.getCode(),
                institution.getName()
        );
    }

    @Transactional
    public void signup(SignupRequest request) {
        Institution institution = institutionRepository.findByCode(request.institutionCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 기관 코드입니다."));

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (!Boolean.TRUE.equals(request.agreedTerms())) {
            throw new IllegalArgumentException("약관 동의가 필요합니다.");
        }

        User user = User.builder()
                .institution(institution)
                .name(request.name())
                .phone(request.phone())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .agreedTerms(request.agreedTerms())
                .isActive(true)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Institution institution = institutionRepository.findByCode(request.institutionCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 기관 코드입니다."));

        User user = userRepository.findByInstitutionAndEmail(institution, request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(
                accessToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                institution.getId(),
                institution.getCode(),
                institution.getName(),
                Boolean.TRUE.equals(user.getMustChangePassword())
        );
    }

    public MeResponse me(String token) {
        String email = jwtTokenProvider.getEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Institution institution = user.getInstitution();

        return new MeResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                institution.getId(),
                institution.getCode(),
                institution.getName(),
                Boolean.TRUE.equals(user.getMustChangePassword())
        );
    }
}
