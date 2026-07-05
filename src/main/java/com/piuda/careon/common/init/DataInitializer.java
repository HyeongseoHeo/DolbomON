package com.piuda.careon.common.init;

import com.piuda.careon.institution.entity.Institution;
import com.piuda.careon.institution.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final InstitutionRepository institutionRepository;

    @Override
    public void run(String... args) {
        String code = "CJ-2024-0011";

        if (institutionRepository.findByCode(code).isEmpty()) {
            Institution institution = Institution.builder()
                    .name("청주 복지관")
                    .code(code)
                    .address("충청북도 청주시 흥덕구 복지로 123")
                    .phone("043-000-0000")
                    .build();

            institutionRepository.save(institution);
        }
    }
}
