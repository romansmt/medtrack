import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { useAvailabilityCheckMutation, type PharmacyAvailabilityResult } from "../api/availability";
import type { Drug } from "../api/drugs";
import { useCreateReservationMutation } from "../api/reservations";
import { usePatient } from "../context/PatientContext";
import { useUserLocation } from "../context/UserLocationContext";
import { DrugSearchCombobox } from "../components/DrugSearchCombobox";
import { LocationPicker } from "../components/LocationPicker";
import { PharmacyCard } from "../components/PharmacyCard";
import { PharmacyMap } from "../components/PharmacyMap";
import { formatDateTime, formatPrice } from "../utils/format";
import "./SearchPage.css";

interface SelectionItem {
  drug: Drug;
  quantity: number;
}

export function SearchPage() {
  const [searchParams] = useSearchParams();
  const { location } = useUserLocation();
  const { selectedPatient } = usePatient();
  const [selection, setSelection] = useState<SelectionItem[]>([]);
  const [results, setResults] = useState<PharmacyAvailabilityResult[] | null>(null);
  const availabilityCheck = useAvailabilityCheckMutation();
  const reserve = useCreateReservationMutation(selectedPatient?.svnr);
  const [reservedKey, setReservedKey] = useState<string | null>(null);

  const addDrug = (drug: Drug) => {
    setSelection((prev) => {
      const existing = prev.find((item) => item.drug.id === drug.id);
      if (existing) {
        return prev.map((item) => (item.drug.id === drug.id ? { ...item, quantity: item.quantity + 1 } : item));
      }
      return [...prev, { drug, quantity: 1 }];
    });
  };

  const updateQuantity = (drugId: number, delta: number) => {
    setSelection((prev) =>
      prev
        .map((item) => (item.drug.id === drugId ? { ...item, quantity: item.quantity + delta } : item))
        .filter((item) => item.quantity > 0),
    );
  };

  const removeDrug = (drugId: number) => {
    setSelection((prev) => prev.filter((item) => item.drug.id !== drugId));
  };

  const runCheck = () => {
    if (!location || selection.length === 0) return;
    availabilityCheck.mutate(
      {
        items: selection.map((item) => ({ drugId: item.drug.id, quantity: item.quantity })),
        latitude: location.lat,
        longitude: location.lng,
      },
      { onSuccess: (data) => setResults(data) },
    );
  };

  const handleReserve = (pharmacyId: number, drugId: number, quantity: number) => {
    const key = `${pharmacyId}-${drugId}`;
    reserve.mutate(
      { pharmacyId, drugId, quantity },
      { onSuccess: () => setReservedKey(key) },
    );
  };

  return (
    <section className="search-page">
      <h1>Suche & Verfügbarkeit</h1>
      {searchParams.get("q") && (
        <p className="search-page__hint">Ausgangssuche von der Startseite: "{searchParams.get("q")}"</p>
      )}

      <LocationPicker />

      <DrugSearchCombobox onSelect={addDrug} placeholder="Medikament zur Auswahlliste hinzufügen…" />

      {selection.length > 0 && (
        <ul className="search-page__selection">
          {selection.map((item) => (
            <li key={item.drug.id} className="selection-row">
              <div className="selection-row__info">
                <span className="selection-row__name">{item.drug.name}</span>
                <span className="selection-row__meta">
                  {item.drug.packSize} · {item.drug.manufacturer}
                </span>
              </div>
              <div className="selection-row__quantity">
                <button type="button" onClick={() => updateQuantity(item.drug.id, -1)} aria-label="Weniger">
                  −
                </button>
                <span>{item.quantity}</span>
                <button type="button" onClick={() => updateQuantity(item.drug.id, 1)} aria-label="Mehr">
                  +
                </button>
              </div>
              <Link to={`/drugs/${item.drug.id}/compare`} className="selection-row__compare">
                Preisvergleich
              </Link>
              <button
                type="button"
                className="selection-row__remove"
                onClick={() => removeDrug(item.drug.id)}
                aria-label="Entfernen"
              >
                ×
              </button>
            </li>
          ))}
        </ul>
      )}

      <button
        type="button"
        className="search-page__check-button"
        disabled={!location || selection.length === 0 || availabilityCheck.isPending}
        onClick={runCheck}
      >
        {availabilityCheck.isPending ? "Prüfe…" : "Verfügbarkeit prüfen"}
      </button>

      {results && (
        <div className="search-page__results">
          <div className="search-page__list">
            {results.length === 0 && <p className="search-page__hint">Keine Apotheken in der Nähe gefunden.</p>}
            {results.map((result) => {
              const freshest = result.items
                .map((i) => i.lastUpdated)
                .filter((v): v is string => v !== null)
                .sort()
                .at(-1);

              return (
                <PharmacyCard key={result.pharmacy.id} pharmacy={result.pharmacy}>
                  <div className="search-page__availability-badge">
                    {result.allAvailable ? (
                      <span className="search-page__badge search-page__badge--ok">Alles verfügbar</span>
                    ) : (
                      <span className="search-page__badge search-page__badge--partial">
                        {result.items.filter((i) => i.inStock).length}/{result.items.length} verfügbar
                      </span>
                    )}
                  </div>

                  <ul className="search-page__item-results">
                    {result.items.map((item) => {
                      const key = `${result.pharmacy.id}-${item.drugId}`;
                      const canReserve = result.pharmacy.reservationSupported && item.inStock;
                      return (
                        <li key={item.drugId} className="item-result-row">
                          <div className="item-result-row__info">
                            <span>{item.drugName}</span>
                            <span className={item.inStock ? "item-result-row__ok" : "item-result-row__no"}>
                              {item.inStock ? `✓ Verfügbar · ${formatPrice(item.price)}` : "✗ Nicht verfügbar"}
                            </span>
                          </div>
                          {canReserve &&
                            (reservedKey === key ? (
                              <span className="item-result-row__reserved">Reserviert</span>
                            ) : (
                              <button
                                type="button"
                                onClick={() => handleReserve(result.pharmacy.id, item.drugId, item.requestedQuantity)}
                                disabled={reserve.isPending}
                              >
                                Reservieren
                              </button>
                            ))}
                        </li>
                      );
                    })}
                  </ul>

                  {!result.pharmacy.reservationSupported && (
                    <p className="search-page__no-reservation">Reservierung wird nicht angeboten</p>
                  )}

                  {freshest && (
                    <p className="search-page__freshness">
                      Datenübermittlung: Aktiv · zuletzt übermittelt {formatDateTime(freshest)}
                    </p>
                  )}

                  <button type="button" className="search-page__recheck" onClick={runCheck}>
                    Erneut prüfen
                  </button>
                </PharmacyCard>
              );
            })}
          </div>

          {location && results.length > 0 && (
            <div className="search-page__map">
              <PharmacyMap pharmacies={results.map((r) => r.pharmacy)} center={location} />
            </div>
          )}
        </div>
      )}
    </section>
  );
}
