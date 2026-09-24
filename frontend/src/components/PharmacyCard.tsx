import { useState, type ReactNode } from "react";
import { usePharmacyDetailQuery, type Pharmacy } from "../api/pharmacies";
import { formatDistance } from "../utils/format";
import { FavoriteButton } from "./FavoriteButton";
import { PharmacyContactActions } from "./PharmacyContactActions";
import { PharmacyHours } from "./PharmacyHours";
import "./PharmacyCard.css";

export function PharmacyCard({
  pharmacy,
  isFavorite,
  onToggleFavorite,
  favoritePending,
  children,
}: {
  pharmacy: Pharmacy;
  isFavorite?: boolean;
  onToggleFavorite?: () => void;
  favoritePending?: boolean;
  children?: ReactNode;
}) {
  const [expanded, setExpanded] = useState(false);
  const detail = usePharmacyDetailQuery(expanded ? pharmacy.id : undefined);

  return (
    <div className="pharmacy-card">
      <div className="pharmacy-card__header">
        <button
          type="button"
          className="pharmacy-card__toggle"
          onClick={() => setExpanded((e) => !e)}
          aria-expanded={expanded}
        >
          <div className="pharmacy-card__title">
            <span className="pharmacy-card__name">{pharmacy.name}</span>
            <span className="pharmacy-card__meta">
              {formatDistance(pharmacy.distanceKm)}
              {pharmacy.distanceKm !== null && " · "}
              {pharmacy.onCallNow ? (
                <span className="pharmacy-card__status pharmacy-card__status--oncall">Bereitschaftsdienst</span>
              ) : pharmacy.openNow ? (
                <span className="pharmacy-card__status pharmacy-card__status--open">geöffnet</span>
              ) : (
                <span className="pharmacy-card__status pharmacy-card__status--closed">geschlossen</span>
              )}
            </span>
          </div>
          <span className={"pharmacy-card__chevron" + (expanded ? " is-open" : "")}>⌄</span>
        </button>
        {onToggleFavorite && (
          <FavoriteButton isFavorite={!!isFavorite} onToggle={onToggleFavorite} pending={favoritePending} />
        )}
      </div>

      {expanded && (
        <div className="pharmacy-card__body">
          <p className="pharmacy-card__address">{pharmacy.address}</p>
          <PharmacyContactActions pharmacy={pharmacy} />
          {detail.isLoading && <p className="pharmacy-card__loading">Lade Öffnungszeiten…</p>}
          {detail.data && <PharmacyHours hours={detail.data.openingHours} />}
          {children}
        </div>
      )}
    </div>
  );
}
