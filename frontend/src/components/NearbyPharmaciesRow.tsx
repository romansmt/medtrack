import { Link } from "react-router-dom";
import { useNearbyPharmaciesQuery } from "../api/pharmacies";
import { useUserLocation } from "../context/UserLocationContext";
import { Icon } from "../layout/Icon";
import "./NearbyPharmaciesRow.css";

const ROW_LIMIT = 8;

// There's no per-pharmacy detail route (PharmaciesPage itself handles expand/collapse via
// PharmacyCard) - every card here links to the list page, same as the quick-link tile above it.
export function NearbyPharmaciesRow() {
  const { location } = useUserLocation();
  const { data, isLoading, isError } = useNearbyPharmaciesQuery(location?.lat, location?.lng);

  return (
    <section className="nearby-row">
      <div className="section-header">
        <h2>Apotheken in der Nähe</h2>
        <Link to="/pharmacies" className="section-header__link">
          Alle ansehen
        </Link>
      </div>

      {!location && <p className="nearby-row__hint">Standort festlegen, um Apotheken in Ihrer Nähe zu sehen.</p>}
      {location && isLoading && <p className="nearby-row__hint">Lade Apotheken…</p>}
      {location && isError && <p className="nearby-row__hint">Apotheken nicht verfügbar.</p>}
      {location && data && data.length === 0 && (
        <p className="nearby-row__hint">Keine Apotheken in der Nähe gefunden.</p>
      )}

      {location && data && data.length > 0 && (
        <div className="scroll-row">
          {data.slice(0, ROW_LIMIT).map((pharmacy) => (
            <Link to="/pharmacies" key={pharmacy.id} className="nearby-card">
              <span className="nearby-card__icon">
                <Icon name="store" size={20} />
              </span>
              <span className="nearby-card__name">{pharmacy.name}</span>
              <span className="nearby-card__meta">
                {pharmacy.distanceKm !== null ? `${Math.round(pharmacy.distanceKm * 1000)} m entfernt` : pharmacy.address}
              </span>
              <span
                className={
                  "nearby-card__status " +
                  (pharmacy.onCallNow
                    ? "nearby-card__status--oncall"
                    : pharmacy.openNow
                      ? "nearby-card__status--open"
                      : "nearby-card__status--closed")
                }
              >
                {pharmacy.onCallNow ? "Notdienst" : pharmacy.openNow ? "Geöffnet" : "Geschlossen"}
              </span>
            </Link>
          ))}
        </div>
      )}
    </section>
  );
}
