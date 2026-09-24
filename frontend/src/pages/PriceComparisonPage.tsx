import { Link, useParams } from "react-router-dom";
import { useDrugQuery, usePriceComparisonQuery } from "../api/drugs";
import { useUserLocation } from "../context/UserLocationContext";
import { LocationPicker } from "../components/LocationPicker";
import { formatDistance, formatPrice } from "../utils/format";
import "./PriceComparisonPage.css";

export function PriceComparisonPage() {
  const { drugId } = useParams<{ drugId: string }>();
  const id = drugId ? Number(drugId) : undefined;
  const { location } = useUserLocation();

  const drug = useDrugQuery(id);
  const comparison = usePriceComparisonQuery(id, location?.lat, location?.lng);

  return (
    <section className="price-comparison-page">
      <Link to="/search" className="price-comparison-page__back">
        ← Zurück zur Suche
      </Link>

      <h1>{drug.data ? `Preisvergleich: ${drug.data.name}` : "Preisvergleich"}</h1>
      {drug.data && (
        <p className="price-comparison-page__meta">
          {drug.data.form}, {drug.data.packSize} · {drug.data.manufacturer}
        </p>
      )}

      <LocationPicker />

      {!location && <p className="price-comparison-page__hint">Standort festlegen, um Preise zu vergleichen.</p>}
      {location && comparison.isLoading && <p className="price-comparison-page__hint">Lade Preise…</p>}

      {comparison.data && (
        <ul className="price-comparison-page__list">
          {comparison.data.map((entry, i) => (
            <li key={entry.pharmacy.id} className="price-comparison-row">
              {i === 0 && entry.inStock && <span className="price-comparison-row__badge">Günstigster Preis</span>}
              <div className="price-comparison-row__info">
                <span className="price-comparison-row__name">{entry.pharmacy.name}</span>
                <span className="price-comparison-row__distance">{formatDistance(entry.pharmacy.distanceKm)}</span>
              </div>
              <div className="price-comparison-row__price">
                <span>{formatPrice(entry.price)}</span>
                <span className={entry.inStock ? "price-comparison-row__ok" : "price-comparison-row__no"}>
                  {entry.inStock ? "Verfügbar" : "Nicht verfügbar"}
                </span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
