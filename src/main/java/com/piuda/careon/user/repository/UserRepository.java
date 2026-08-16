package com.piuda.careon.user.repository;

import com.piuda.careon.institution.entity.Institution;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByInstitutionAndEmail(
            Institution institution,
            String email
    );

    boolean existsByEmail(String email);

    List<User> findByInstitutionOrderByCreatedAtDesc(
            Institution institution
    );

    List<User> findByInstitutionAndRole(
            Institution institution,
            UserRole role
    );
}