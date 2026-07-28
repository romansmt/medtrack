package com.medtrack.application.service;

import com.medtrack.application.dto.DrugResponse;
import com.medtrack.application.dto.PharmacyResponse;
import com.medtrack.domain.Drug;
import com.medtrack.domain.FavoriteDrug;
import com.medtrack.domain.FavoritePharmacy;
import com.medtrack.domain.Patient;
import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.domain.Pharmacy;
import com.medtrack.infrastructure.persistence.DrugRepository;
import com.medtrack.infrastructure.persistence.FavoriteDrugRepository;
import com.medtrack.infrastructure.persistence.FavoritePharmacyRepository;
import com.medtrack.infrastructure.persistence.PatientRepository;
import com.medtrack.infrastructure.persistence.PharmacyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FavoriteService {

    private final PatientRepository patientRepository;
    private final PharmacyRepository pharmacyRepository;
    private final DrugRepository drugRepository;
    private final FavoritePharmacyRepository favoritePharmacyRepository;
    private final FavoriteDrugRepository favoriteDrugRepository;
    private final PharmacyService pharmacyService;
    private final DrugService drugService;

    public FavoriteService(PatientRepository patientRepository, PharmacyRepository pharmacyRepository,
                            DrugRepository drugRepository, FavoritePharmacyRepository favoritePharmacyRepository,
                            FavoriteDrugRepository favoriteDrugRepository, PharmacyService pharmacyService,
                            DrugService drugService) {
        this.patientRepository = patientRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.drugRepository = drugRepository;
        this.favoritePharmacyRepository = favoritePharmacyRepository;
        this.favoriteDrugRepository = favoriteDrugRepository;
        this.pharmacyService = pharmacyService;
        this.drugService = drugService;
    }

    @Transactional(readOnly = true)
    public List<PharmacyResponse> listFavoritePharmacies(String svnr) {
        Patient patient = findPatientOrThrow(svnr);
        List<Long> pharmacyIds = favoritePharmacyRepository.findByPatientId(patient.getId()).stream()
                .map(favorite -> favorite.getPharmacy().getId())
                .toList();
        return pharmacyService.findByIds(pharmacyIds);
    }

    @Transactional
    public void addFavoritePharmacy(String svnr, Long pharmacyId) {
        Patient patient = findPatientOrThrow(svnr);
        if (favoritePharmacyRepository.existsByPatientIdAndPharmacyId(patient.getId(), pharmacyId)) {
            return;
        }
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new NoSuchElementException("No pharmacy for id " + pharmacyId));
        favoritePharmacyRepository.save(new FavoritePharmacy(patient, pharmacy, Instant.now()));
    }

    @Transactional
    public void removeFavoritePharmacy(String svnr, Long pharmacyId) {
        Patient patient = findPatientOrThrow(svnr);
        favoritePharmacyRepository.deleteByPatientIdAndPharmacyId(patient.getId(), pharmacyId);
    }

    @Transactional(readOnly = true)
    public List<DrugResponse> listFavoriteDrugs(String svnr) {
        Patient patient = findPatientOrThrow(svnr);
        List<Long> drugIds = favoriteDrugRepository.findByPatientId(patient.getId()).stream()
                .map(favorite -> favorite.getDrug().getId())
                .toList();
        return drugService.getByIds(drugIds);
    }

    @Transactional
    public void addFavoriteDrug(String svnr, Long drugId) {
        Patient patient = findPatientOrThrow(svnr);
        if (favoriteDrugRepository.existsByPatientIdAndDrugId(patient.getId(), drugId)) {
            return;
        }
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new NoSuchElementException("No drug for id " + drugId));
        favoriteDrugRepository.save(new FavoriteDrug(patient, drug, Instant.now()));
    }

    @Transactional
    public void removeFavoriteDrug(String svnr, Long drugId) {
        Patient patient = findPatientOrThrow(svnr);
        favoriteDrugRepository.deleteByPatientIdAndDrugId(patient.getId(), drugId);
    }

    private Patient findPatientOrThrow(String svnr) {
        return patientRepository.findBySvnr(svnr)
                .orElseThrow(() -> new PatientNotFoundException(svnr));
    }
}
