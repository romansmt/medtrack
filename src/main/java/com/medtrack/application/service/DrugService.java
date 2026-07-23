package com.medtrack.application.service;

import com.medtrack.application.dto.DrugLeafletResponse;
import com.medtrack.application.dto.DrugResponse;
import com.medtrack.domain.Drug;
import com.medtrack.infrastructure.persistence.DrugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DrugService {

    private final DrugRepository drugRepository;

    public DrugService(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    @Transactional(readOnly = true)
    public List<DrugResponse> search(String query) {
        return drugRepository.search(query).stream()
                .map(DrugService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DrugResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public DrugLeafletResponse getLeaflet(Long id) {
        Drug drug = findOrThrow(id);
        return new DrugLeafletResponse(drug.getId(), drug.getName(), drug.getPackageLeafletText());
    }

    private Drug findOrThrow(Long id) {
        return drugRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No drug for id " + id));
    }

    private static DrugResponse toResponse(Drug drug) {
        return new DrugResponse(drug.getId(), drug.getName(), drug.getActiveSubstance(), drug.getForm(),
                drug.getPackSize(), drug.getManufacturer(), drug.isPrescriptionRequired(), drug.getPzn());
    }
}
