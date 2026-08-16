package com.piuda.careon.user.controller;

import com.piuda.careon.user.dto.ChangePasswordRequest;
import com.piuda.careon.user.dto.CreateUserRequest;
import com.piuda.careon.user.dto.ResetPasswordResponse;
import com.piuda.careon.user.dto.UserResponse;
import com.piuda.careon.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 관리자 - 기관 사용자 추가
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            Authentication authentication,
            @RequestBody CreateUserRequest request
    ) {

        String adminEmail = authentication.getName();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        userService.createUser(
                                adminEmail,
                                request
                        )
                );
    }

    /**
     * 같은 기관 사용자 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                userService.getInstitutionUsers(userEmail)
        );
    }

    /**
     * 사용자 단건 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            Authentication authentication,
            @PathVariable UUID userId
    ) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                userService.getUser(
                        userEmail,
                        userId
                )
        );
    }

    /**
     * 본인 비밀번호 변경
     */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request
    ) {

        String userEmail = authentication.getName();

        userService.changeMyPassword(
                userEmail,
                request
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * 관리자 - 사용자 비밀번호 초기화
     */
    @PatchMapping("/{userId}/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetUserPassword(
            Authentication authentication,
            @PathVariable UUID userId
    ) {

        String adminEmail = authentication.getName();

        return ResponseEntity.ok(
                userService.resetUserPassword(
                        adminEmail,
                        userId
                )
        );
    }
}