package com.medtrack.api.controller;

import com.medtrack.application.dto.DrugResponse;
import com.medtrack.application.dto.PharmacyResponse;
import com.medtrack.application.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{svnr}")
@Tag(name = "Favorites", description = "A patient's favorite pharmacies and medications")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping("/favorite-pharmacies")
    @Operation(summary = "List a patient's favorite pharmacies")
    public List<PharmacyResponse> listFavoritePharmacies(@PathVariable String svnr) {
        return favoriteService.listFavoritePharmacies(svnr);
    }

    @PostMapping("/favorite-pharmacies/{pharmacyId}")
    @Operation(summary = "Add a pharmacy to a patient's favorites (idempotent)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFavoritePharmacy(@PathVariable String svnr, @PathVariable Long pharmacyId) {
        favoriteService.addFavoritePharmacy(svnr, pharmacyId);
    }

    @DeleteMapping("/favorite-pharmacies/{pharmacyId}")
    @Operation(summary = "Remove a pharmacy from a patient's favorites (idempotent)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavoritePharmacy(@PathVariable String svnr, @PathVariable Long pharmacyId) {
        favoriteService.removeFavoritePharmacy(svnr, pharmacyId);
    }

    @GetMapping("/favorite-drugs")
    @Operation(summary = "List a patient's favorite medications")
    public List<DrugResponse> listFavoriteDrugs(@PathVariable String svnr) {
        return favoriteService.listFavoriteDrugs(svnr);
    }

    @PostMapping("/favorite-drugs/{drugId}")
    @Operation(summary = "Add a medication to a patient's favorites (idempotent)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFavoriteDrug(@PathVariable String svnr, @PathVariable Long drugId) {
        favoriteService.addFavoriteDrug(svnr, drugId);
    }

    @DeleteMapping("/favorite-drugs/{drugId}")
    @Operation(summary = "Remove a medication from a patient's favorites (idempotent)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavoriteDrug(@PathVariable String svnr, @PathVariable Long drugId) {
        favoriteService.removeFavoriteDrug(svnr, drugId);
    }
}
