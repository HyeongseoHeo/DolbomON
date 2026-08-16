package com.piuda.careon.user.entity;

import com.piuda.careon.institution.entity.Institution;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false)
    private Boolean agreedTerms;

    @Column(nullable = false)
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean mustChangePassword;

    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.isActive == null) {
            this.isActive = true;
        }

        if (this.employmentStatus == null) {
            this.employmentStatus = EmploymentStatus.ACTIVE;
        }

        if (this.agreedTerms == null) {
            this.agreedTerms = false;
        }

        if (this.mustChangePassword == null) {
            this.mustChangePassword = false;
        }

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
        this.mustChangePassword = false;
    }

    public void resetPassword(String encodedTemporaryPassword) {
        this.passwordHash = encodedTemporaryPassword;
        this.mustChangePassword = true;
    }

    public void changeEmploymentStatus(EmploymentStatus status) {
        this.employmentStatus = status;

        if (status == EmploymentStatus.ACTIVE) {
            this.isActive = true;
        } else {
            this.isActive = false;
        }
    }

    public void resign() {
        this.employmentStatus = EmploymentStatus.RESIGNED;
        this.isActive = false;
    }
}
