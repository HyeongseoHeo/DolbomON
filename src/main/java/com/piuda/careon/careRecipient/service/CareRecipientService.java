package com.piuda.careon.careRecipient.service;

import com.piuda.careon.careRecipient.dto.CareRecipientResponse;
import com.piuda.careon.careRecipient.dto.CreateCareRecipientRequest;
import com.piuda.careon.careRecipient.entity.CareRecipient;
import com.piuda.careon.careRecipient.repository.CareRecipientRepository;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.repository.UserRepository;
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
    public CareRecipientResponse create(CreateCareRecipientRequest request) {

        User caregiver = userRepository.findById(request.caregiverId())
                .orElseThrow(() -> new IllegalArgumentException("생활지원사를 찾을 수 없습니다."));

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
                .caregiver(caregiver)
                .build();

        careRecipientRepository.save(recipient);

        return toResponse(recipient);
    }

    /**
     * 대상자 단건 조회
     */
    @Transactional(readOnly = true)
    public CareRecipientResponse getRecipient(UUID id) {

        CareRecipient recipient = careRecipientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("대상자를 찾을 수 없습니다."));

        return toResponse(recipient);
    }

    /**
     * 담당 생활지원사 대상자 목록
     */
    @Transactional(readOnly = true)
    public List<CareRecipientResponse> getRecipientsByCaregiver(UUID caregiverId) {

        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new IllegalArgumentException("생활지원사를 찾을 수 없습니다."));

        return careRecipientRepository.findByCaregiver(caregiver)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 대상자 삭제
     */
    public void delete(UUID id) {

        CareRecipient recipient = careRecipientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("대상자를 찾을 수 없습니다."));

        careRecipientRepository.delete(recipient);
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
                recipient.getCaregiver().getName()
        );
    }
}
