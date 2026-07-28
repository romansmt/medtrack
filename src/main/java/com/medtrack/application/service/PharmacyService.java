package com.medtrack.application.service;

import com.medtrack.application.dto.OpeningHoursResponse;
import com.medtrack.application.dto.PharmacyDetailResponse;
import com.medtrack.application.dto.PharmacyResponse;
import com.medtrack.application.port.GeoPort;
import com.medtrack.domain.Coordinates;
import com.medtrack.domain.OpeningHoursKind;
import com.medtrack.domain.Pharmacy;
import com.medtrack.domain.PharmacyOpeningHours;
import com.medtrack.infrastructure.persistence.PharmacyOpeningHoursRepository;
import com.medtrack.infrastructure.persistence.PharmacyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyOpeningHoursRepository openingHoursRepository;
    private final GeoPort geoPort;

    public PharmacyService(PharmacyRepository pharmacyRepository,
                            PharmacyOpeningHoursRepository openingHoursRepository,
                            GeoPort geoPort) {
        this.pharmacyRepository = pharmacyRepository;
        this.openingHoursRepository = openingHoursRepository;
        this.geoPort = geoPort;
    }

    @Transactional(readOnly = true)
    public List<PharmacyResponse> findNearby(Coordinates origin, double radiusKm) {
        return allWithDistance(origin).stream()
                .filter(r -> r.distanceKm() <= radiusKm)
                .sorted(Comparator.comparing(PharmacyResponse::distanceKm))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PharmacyResponse> findOnCallNow(Coordinates origin) {
        return allWithDistance(origin).stream()
                .filter(PharmacyResponse::onCallNow)
                .sorted(Comparator.comparing(PharmacyResponse::distanceKm))
                .toList();
    }

    @Transactional(readOnly = true)
    public PharmacyDetailResponse getDetail(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new NoSuchElementException("No pharmacy for id " + pharmacyId));
        List<PharmacyOpeningHours> hours = openingHoursRepository.findByPharmacyId(pharmacyId);

        PharmacyResponse response = toResponse(pharmacy, hours, null, LocalDateTime.now());
        List<OpeningHoursResponse> hoursResponse = hours.stream()
                .sorted(Comparator.comparing(PharmacyOpeningHours::getDayOfWeek)
                        .thenComparing(PharmacyOpeningHours::getOpensAt))
                .map(h -> new OpeningHoursResponse(h.getDayOfWeek().toString(), h.getOpensAt(), h.getClosesAt(),
                        h.getKind().toString()))
                .toList();

        return new PharmacyDetailResponse(response, hoursResponse);
    }

    public Coordinates geocode(String address) {
        return geoPort.geocode(address)
                .orElseThrow(() -> new NoSuchElementException("Could not resolve address: " + address));
    }

    @Transactional(readOnly = true)
    public List<PharmacyResponse> findByIds(List<Long> pharmacyIds) {
        List<Pharmacy> pharmacies = pharmacyRepository.findAllById(pharmacyIds);
        Map<Long, List<PharmacyOpeningHours>> hoursByPharmacy = openingHoursRepository.findByPharmacyIdIn(pharmacyIds)
                .stream()
                .collect(Collectors.groupingBy(h -> h.getPharmacy().getId()));
        LocalDateTime now = LocalDateTime.now();

        return pharmacies.stream()
                .map(p -> toResponse(p, hoursByPharmacy.getOrDefault(p.getId(), List.of()), null, now))
                .toList();
    }

    private List<PharmacyResponse> allWithDistance(Coordinates origin) {
        List<Pharmacy> pharmacies = pharmacyRepository.findAll();
        List<Long> ids = pharmacies.stream().map(Pharmacy::getId).toList();
        Map<Long, List<PharmacyOpeningHours>> hoursByPharmacy = openingHoursRepository.findByPharmacyIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(h -> h.getPharmacy().getId()));
        LocalDateTime now = LocalDateTime.now();

        return pharmacies.stream()
                .map(p -> toResponse(p, hoursByPharmacy.getOrDefault(p.getId(), List.of()), origin, now))
                .toList();
    }

    private PharmacyResponse toResponse(Pharmacy pharmacy, List<PharmacyOpeningHours> hours,
                                         Coordinates origin, LocalDateTime now) {
        Double distanceKm = origin == null ? null : DistanceCalculator.distanceKm(origin,
                new Coordinates(pharmacy.getLatitude(), pharmacy.getLongitude()));

        boolean openNow = matchesNow(hours, now, OpeningHoursKind.REGULAR);
        boolean onCallNow = matchesNow(hours, now, OpeningHoursKind.ON_CALL);

        return new PharmacyResponse(pharmacy.getId(), pharmacy.getName(), pharmacy.getAddress(),
                pharmacy.getLatitude(), pharmacy.getLongitude(), pharmacy.isWheelchairAccessible(),
                pharmacy.getPhone(), pharmacy.getEmail(), pharmacy.getWebsite(),
                pharmacy.isReservationSupported(), distanceKm, openNow, onCallNow);
    }

    private boolean matchesNow(List<PharmacyOpeningHours> hours, LocalDateTime now, OpeningHoursKind kind) {
        DayOfWeek today = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        return hours.stream()
                .filter(h -> h.getKind() == kind && h.getDayOfWeek() == today)
                .anyMatch(h -> !time.isBefore(h.getOpensAt()) && !time.isAfter(h.getClosesAt()));
    }
}
