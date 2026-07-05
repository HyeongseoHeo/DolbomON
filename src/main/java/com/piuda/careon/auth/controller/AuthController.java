package com.piuda.careon.auth.controller;

import com.piuda.careon.auth.dto.*;
import com.piuda.careon.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 기관 코드 확인
     */
    @PostMapping("/check-institution")
    public ResponseEntity<CheckInstitutionResponse> checkInstitution(
            @RequestBody CheckInstitutionRequest request
    ) {

        return ResponseEntity.ok(
                authService.checkInstitution(request)
        );
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(
            @RequestBody SignupRequest request
    ) {

        authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "");

        return ResponseEntity.ok(
                authService.me(token)
        );
    }
}
