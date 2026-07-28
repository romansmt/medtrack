package com.medtrack.application.service;

import com.medtrack.application.dto.PharmacyResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PharmacyRepository pharmacyRepository;
    @Mock
    private DrugRepository drugRepository;
    @Mock
    private FavoritePharmacyRepository favoritePharmacyRepository;
    @Mock
    private FavoriteDrugRepository favoriteDrugRepository;
    @Mock
    private PharmacyService pharmacyService;
    @Mock
    private DrugService drugService;

    @Mock
    private Patient patient;
    @Mock
    private Pharmacy pharmacy;

    private FavoriteService newService() {
        return new FavoriteService(patientRepository, pharmacyRepository, drugRepository,
                favoritePharmacyRepository, favoriteDrugRepository, pharmacyService, drugService);
    }

    @Test
    void addFavoritePharmacyIsIdempotentWhenAlreadyFavorited() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        when(favoritePharmacyRepository.existsByPatientIdAndPharmacyId(1L, 5L)).thenReturn(true);

        newService().addFavoritePharmacy(svnr, 5L);

        verify(favoritePharmacyRepository, never()).save(any());
        verify(pharmacyRepository, never()).findById(any());
    }

    @Test
    void addFavoritePharmacySavesWhenNotYetFavorited() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        when(favoritePharmacyRepository.existsByPatientIdAndPharmacyId(1L, 5L)).thenReturn(false);
        when(pharmacyRepository.findById(5L)).thenReturn(Optional.of(pharmacy));

        newService().addFavoritePharmacy(svnr, 5L);

        verify(favoritePharmacyRepository).save(any(FavoritePharmacy.class));
    }

    @Test
    void addFavoritePharmacyPropagatesNotFoundForUnknownSvnrWithoutQueryingFavorites() {
        String svnr = "0000000000";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().addFavoritePharmacy(svnr, 5L))
                .isInstanceOf(PatientNotFoundException.class);
        verify(favoritePharmacyRepository, never()).existsByPatientIdAndPharmacyId(any(), any());
    }

    @Test
    void removeFavoritePharmacyDelegatesToRepository() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);

        newService().removeFavoritePharmacy(svnr, 5L);

        verify(favoritePharmacyRepository).deleteByPatientIdAndPharmacyId(1L, 5L);
    }

    @Test
    void listFavoritePharmaciesResolvesIdsThenDelegatesToPharmacyService() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        when(pharmacy.getId()).thenReturn(5L);
        FavoritePharmacy favorite = new FavoritePharmacy(patient, pharmacy, Instant.now());
        when(favoritePharmacyRepository.findByPatientId(1L)).thenReturn(List.of(favorite));

        PharmacyResponse response = new PharmacyResponse(5L, "Test Pharmacy", "Test Address", 48.2, 16.3,
                true, "+43 1 0000000", "test@example.example", null, true, null, false, false);
        when(pharmacyService.findByIds(List.of(5L))).thenReturn(List.of(response));

        List<PharmacyResponse> result = newService().listFavoritePharmacies(svnr);

        assertThat(result).containsExactly(response);
    }

    @Test
    void addFavoriteDrugIsIdempotentWhenAlreadyFavorited() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        when(favoriteDrugRepository.existsByPatientIdAndDrugId(1L, 3L)).thenReturn(true);

        newService().addFavoriteDrug(svnr, 3L);

        verify(favoriteDrugRepository, never()).save(any(FavoriteDrug.class));
    }
}
