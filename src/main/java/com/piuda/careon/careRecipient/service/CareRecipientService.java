package com.piuda.careon.careRecipient.service;

import com.piuda.careon.careRecipient.dto.CareRecipientResponse;
import com.piuda.careon.careRecipient.dto.CreateCareRecipientRequest;
import com.piuda.careon.careRecipient.entity.CareRecipient;
import com.piuda.careon.careRecipient.repository.CareRecipientRepository;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.repository.UserRepository;
import com.piuda.careon.careRecipient.dto.UpdateCareRecipientRequest;
import com.piuda.careon.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CareRecipientService {

    private final CareRecipientRepository careRecipientRepository;
    private final UserRepository userRepository;

    /**
     * 대상자 등록
     */
    public CareRecipientResponse create(
            String adminEmail,
            CreateCareRecipientRequest request
    ) {

        // 로그인한 관리자 조회
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("관리자 정보를 찾을 수 없습니다.")
                );

        // 요청으로 받은 담당 생활지원사 조회
        User caregiver = userRepository.findById(request.caregiverId())
                .orElseThrow(() ->
                        new IllegalArgumentException("생활지원사를 찾을 수 없습니다.")
                );

        // 실제 생활지원사인지 검증
        if (caregiver.getRole() != UserRole.CARE_WORKER) {
            throw new IllegalArgumentException(
                    "담당자는 생활지원사(CARE_WORKER)만 지정할 수 있습니다."
            );
        }

        // 같은 기관인지 검증
        if (admin.getInstitution() == null ||
                caregiver.getInstitution() == null ||
                !admin.getInstitution().getId()
                        .equals(caregiver.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "같은 기관에 소속된 생활지원사만 지정할 수 있습니다."
            );
        }

        CareRecipient recipient = CareRecipient.builder()
                .name(request.name())
                .age(request.age())
                .gender(request.gender())
                .address(request.address())
                .careLevel(request.careLevel())
                .mainDisease(request.mainDisease())
                .phone(request.phone())
                .familyContactName(request.familyContactName())
                .familyRelation(request.familyRelation())
                .familyContactPhone(request.familyContactPhone())
                .memo(request.memo())
                .institution(admin.getInstitution())
                .caregiver(caregiver)
                .build();

        careRecipientRepository.save(recipient);

        return toResponse(recipient);
    }

    /**
     * 대상자 단건 조회
     */
    @Transactional(readOnly = true)
    public CareRecipientResponse getRecipient(
            String userEmail,
            UUID id
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.")
                );

        CareRecipient recipient = careRecipientRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("대상자를 찾을 수 없습니다.")
                );

        if (currentUser.getInstitution() == null ||
                recipient.getInstitution() == null ||
                !currentUser.getInstitution().getId()
                        .equals(recipient.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "해당 대상자를 조회할 권한이 없습니다."
            );
        }

        return toResponse(recipient);
    }

    /**
     * 담당 생활지원사 대상자 목록
     */
    @Transactional(readOnly = true)
    public List<CareRecipientResponse> getRecipientsByCaregiver(
            String userEmail,
            UUID caregiverId
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.")
                );

        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() ->
                        new IllegalArgumentException("생활지원사를 찾을 수 없습니다.")
                );

        if (caregiver.getRole() != UserRole.CARE_WORKER) {
            throw new IllegalArgumentException(
                    "해당 사용자는 생활지원사가 아닙니다."
            );
        }

        if (currentUser.getInstitution() == null ||
                caregiver.getInstitution() == null ||
                !currentUser.getInstitution().getId()
                        .equals(caregiver.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "다른 기관의 생활지원사 정보는 조회할 수 없습니다."
            );
        }

        return careRecipientRepository.findByCaregiver(caregiver)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 대상자 삭제
     */
    public void delete(
            String userEmail,
            UUID id
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.")
                );

        CareRecipient recipient = careRecipientRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("대상자를 찾을 수 없습니다.")
                );

        if (currentUser.getInstitution() == null ||
                recipient.getInstitution() == null ||
                !currentUser.getInstitution().getId()
                        .equals(recipient.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "해당 대상자를 삭제할 권한이 없습니다."
            );
        }

        careRecipientRepository.delete(recipient);
    }

    /**
     * 기관 전체 조회
     */
    @Transactional(readOnly = true)
    public List<CareRecipientResponse> getInstitutionRecipients(
            String userEmail
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        if (currentUser.getInstitution() == null) {
            throw new IllegalArgumentException(
                    "소속 기관 정보를 찾을 수 없습니다."
            );
        }

        return careRecipientRepository
                .findByInstitutionOrderByCreatedAtDesc(
                        currentUser.getInstitution()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 대상자 정보 수정
     */
    @Transactional
    public CareRecipientResponse update(
            String userEmail,
            UUID recipientId,
            UpdateCareRecipientRequest request
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.")
                );

        CareRecipient recipient = careRecipientRepository.findById(recipientId)
                .orElseThrow(() ->
                        new IllegalArgumentException("대상자를 찾을 수 없습니다.")
                );

        // 같은 기관 대상자인지 확인
        if (currentUser.getInstitution() == null ||
                recipient.getInstitution() == null ||
                !currentUser.getInstitution().getId()
                        .equals(recipient.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "해당 대상자를 수정할 권한이 없습니다."
            );
        }

        User caregiver = userRepository.findById(request.caregiverId())
                .orElseThrow(() ->
                        new IllegalArgumentException("생활지원사를 찾을 수 없습니다.")
                );

        // 담당자는 생활지원사만 가능
        if (caregiver.getRole() != UserRole.CARE_WORKER) {
            throw new IllegalArgumentException(
                    "담당자는 생활지원사(CARE_WORKER)만 지정할 수 있습니다."
            );
        }

        // 같은 기관 생활지원사인지 확인
        if (caregiver.getInstitution() == null ||
                !currentUser.getInstitution().getId()
                        .equals(caregiver.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "같은 기관의 생활지원사만 지정할 수 있습니다."
            );
        }

        recipient.setName(request.name());
        recipient.setAge(request.age());
        recipient.setGender(request.gender());
        recipient.setAddress(request.address());
        recipient.setCareLevel(request.careLevel());
        recipient.setMainDisease(request.mainDisease());
        recipient.setPhone(request.phone());
        recipient.setFamilyContactName(request.familyContactName());
        recipient.setFamilyRelation(request.familyRelation());
        recipient.setFamilyContactPhone(request.familyContactPhone());
        recipient.setMemo(request.memo());
        recipient.setCaregiver(caregiver);

        return toResponse(recipient);
    }

    /**
     * Entity → Response
     */
    private CareRecipientResponse toResponse(CareRecipient recipient) {

        return new CareRecipientResponse(
                recipient.getId(),
                recipient.getName(),
                recipient.getAge(),
                recipient.getGender(),
                recipient.getAddress(),
                recipient.getCareLevel(),
                recipient.getMainDisease(),
                recipient.getPhone(),
                recipient.getFamilyContactName(),
                recipient.getFamilyRelation(),
                recipient.getFamilyContactPhone(),
                recipient.getCaregiver().getId(),
                recipient.getCaregiver().getName(),
                recipient.getMemo()
        );
    }
}
