import type { Pharmacy } from "../api/pharmacies";
import "./PharmacyContactActions.css";

export function PharmacyContactActions({ pharmacy }: { pharmacy: Pharmacy }) {
  const mapsUrl = `https://www.openstreetmap.org/?mlat=${pharmacy.latitude}&mlon=${pharmacy.longitude}#map=18/${pharmacy.latitude}/${pharmacy.longitude}`;

  return (
    <div className="pharmacy-contact-actions">
      <a href={`tel:${pharmacy.phone}`} className="pharmacy-contact-actions__item">
        Anrufen
      </a>
      <a href={mapsUrl} target="_blank" rel="noreferrer" className="pharmacy-contact-actions__item">
        Route
      </a>
      <a href={`mailto:${pharmacy.email}`} className="pharmacy-contact-actions__item">
        Email
      </a>
      {pharmacy.website ? (
        <a href={pharmacy.website} target="_blank" rel="noreferrer" className="pharmacy-contact-actions__item">
          Web
        </a>
      ) : (
        <span className="pharmacy-contact-actions__item pharmacy-contact-actions__item--disabled">Web</span>
      )}
    </div>
  );
}
