import { useNearbyPharmaciesQuery } from "../api/pharmacies";
import { useUserLocation } from "../context/UserLocationContext";
import { Icon } from "../layout/Icon";
import "./NextOpenPharmacyCard.css";

export function NextOpenPharmacyCard() {
  const { location } = useUserLocation();
  const { data, isLoading, isError } = useNearbyPharmaciesQuery(location?.lat, location?.lng);

  if (!location) {
    return (
      <div className="next-open-pharmacy next-open-pharmacy--empty">
        <Icon name="store" size={22} />
        <p>Standort festlegen, um die nächste geöffnete Apotheke zu sehen.</p>
      </div>
    );
  }

  if (isLoading) {
    return <div className="next-open-pharmacy next-open-pharmacy--empty">Lade Apotheken…</div>;
  }

  if (isError) {
    return <div className="next-open-pharmacy next-open-pharmacy--empty">Apotheken nicht verfügbar.</div>;
  }

  const nextOpen = data?.find((pharmacy) => pharmacy.openNow);

  if (!nextOpen) {
    return (
      <div className="next-open-pharmacy next-open-pharmacy--empty">
        <Icon name="store" size={22} />
        <p>Aktuell ist keine Apotheke in der Nähe geöffnet.</p>
      </div>
    );
  }

  return (
    <div className="next-open-pharmacy">
      <span className="next-open-pharmacy__eyebrow">Nächste geöffnete Apotheke</span>
      <div className="next-open-pharmacy__body">
        <Icon name="store" size={22} />
        <div>
          <p className="next-open-pharmacy__name">{nextOpen.name}</p>
          <p className="next-open-pharmacy__meta">
            {nextOpen.distanceKm !== null ? `${Math.round(nextOpen.distanceKm * 1000)} m entfernt` : nextOpen.address}
            {" · "}
            <span className="next-open-pharmacy__status">geöffnet</span>
          </p>
        </div>
      </div>
    </div>
  );
}
