import { useState } from "react";
import {
  useNearbyPharmaciesQuery,
  useOnCallPharmaciesQuery,
} from "../api/pharmacies";
import { useFavoritePharmaciesQuery, useToggleFavoritePharmacyMutation } from "../api/favorites";
import { usePatient } from "../context/PatientContext";
import { useUserLocation } from "../context/UserLocationContext";
import { LocationPicker } from "../components/LocationPicker";
import { PharmacyCard } from "../components/PharmacyCard";
import { PharmacyMap } from "../components/PharmacyMap";
import "./PharmaciesPage.css";

export function PharmaciesPage() {
  const { location } = useUserLocation();
  const { selectedPatient } = usePatient();
  const [onCallOnly, setOnCallOnly] = useState(false);

  const nearby = useNearbyPharmaciesQuery(!onCallOnly ? location?.lat : undefined, !onCallOnly ? location?.lng : undefined);
  const onCall = useOnCallPharmaciesQuery(onCallOnly ? location?.lat : undefined, onCallOnly ? location?.lng : undefined);
  const pharmacies = onCallOnly ? onCall.data : nearby.data;
  const isLoading = onCallOnly ? onCall.isLoading : nearby.isLoading;

  const favorites = useFavoritePharmaciesQuery(selectedPatient?.svnr);
  const favoriteIds = new Set((favorites.data ?? []).map((p) => p.id));
  const toggleFavorite = useToggleFavoritePharmacyMutation(selectedPatient?.svnr);

  return (
    <section className="pharmacies-page">
      <h1>Apotheken</h1>

      <LocationPicker />

      <div className="pharmacies-page__filter">
        <button
          type="button"
          className={!onCallOnly ? "is-active" : ""}
          onClick={() => setOnCallOnly(false)}
        >
          Alle geöffneten
        </button>
        <button
          type="button"
          className={onCallOnly ? "is-active" : ""}
          onClick={() => setOnCallOnly(true)}
        >
          Nur Bereitschaftsdienst
        </button>
      </div>

      {!location && <p className="pharmacies-page__hint">Standort festlegen, um Apotheken in der Nähe zu sehen.</p>}
      {location && isLoading && <p className="pharmacies-page__hint">Lade Apotheken…</p>}
      {location && pharmacies && pharmacies.length === 0 && (
        <p className="pharmacies-page__hint">Keine Apotheken gefunden.</p>
      )}

      {location && pharmacies && pharmacies.length > 0 && (
        <div className="pharmacies-page__results">
          <div className="pharmacies-page__list">
            {pharmacies.map((pharmacy) => (
              <PharmacyCard
                key={pharmacy.id}
                pharmacy={pharmacy}
                isFavorite={favoriteIds.has(pharmacy.id)}
                favoritePending={toggleFavorite.isPending}
                onToggleFavorite={() =>
                  toggleFavorite.mutate({ pharmacyId: pharmacy.id, isFavorite: favoriteIds.has(pharmacy.id) })
                }
              />
            ))}
          </div>
          <div className="pharmacies-page__map">
            <PharmacyMap pharmacies={pharmacies} center={location} />
          </div>
        </div>
      )}
    </section>
  );
}
