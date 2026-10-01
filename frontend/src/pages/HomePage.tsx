import { useMemo } from "react";
import { useNearbyPharmaciesQuery } from "../api/pharmacies";
import { LocationPicker } from "../components/LocationPicker";
import { NearbyPharmaciesRow } from "../components/NearbyPharmaciesRow";
import { PharmacyMap } from "../components/PharmacyMap";
import { PopularMedicationsRow } from "../components/PopularMedicationsRow";
import { QuickLinkTile } from "../components/QuickLinkTile";
import { SearchBar } from "../components/SearchBar";
import { useAdmin } from "../context/AdminContext";
import { usePatient } from "../context/PatientContext";
import { useRegistration } from "../context/RegistrationContext";
import { useUserLocation } from "../context/UserLocationContext";
import "./HomePage.css";

const quickLinks = [
  { to: "/pharmacies", icon: "store", label: "Geöffnete Apotheken in der Nähe" },
  { to: "/search", icon: "search", label: "Verfügbarkeit von Medikamenten" },
  { to: "/medication-plan", icon: "pill", label: "Mein Einnahmeplan" },
  { to: "/favorites", icon: "heart", label: "Meine Lieblingsapotheken" },
] as const;

// The hero map only needs enough pins to look alive, not the full nearby list (NearbyPharmaciesRow
// below fetches and shows more) - capped separately so a dense city center doesn't clutter it.
const HERO_MAP_PHARMACY_LIMIT = 8;

export function HomePage() {
  const { selectedPatient } = usePatient();
  const { isRegistered } = useRegistration();
  const { isAdmin } = useAdmin();
  const { location } = useUserLocation();
  const { data: nearby } = useNearbyPharmaciesQuery(location?.lat, location?.lng);

  const heroMapPharmacies = useMemo(() => (nearby ?? []).slice(0, HERO_MAP_PHARMACY_LIMIT), [nearby]);

  return (
    <section className="home-page">
      {/* Before a real login/registration, selectedPatient still silently defaults to the first
          seeded patient (so the demo pages have data to show) - that default isn't a real identity,
          so this stays unpersonalized until isRegistered. Admin mode is an exception: picking a
          patient from the switcher is a deliberate choice, not a silent default. */}
      {(isRegistered || isAdmin) && selectedPatient && (
        <p className="home-page__welcome">
          Angemeldet als <strong>{selectedPatient.name}</strong> · MedTrack-ID:{" "}
          <strong>{selectedPatient.medtrackId}</strong>
        </p>
      )}

      <div className="home-hero">
        <div className="home-hero__panel">
          <span className="home-hero__eyebrow">Ihre Apotheke, digital</span>
          <h1>
            Österreichs Apotheken.
            <br />
            Immer für Sie da.
          </h1>
          <p>
            Finden Sie geöffnete Apotheken in Ihrer Nähe, prüfen Sie die Verfügbarkeit von
            Medikamenten und behalten Sie Ihren Einnahmeplan im Blick.
          </p>
          <div className="home-hero__controls">
            <SearchBar />
            <LocationPicker />
          </div>
        </div>
        <div className="home-hero__map">
          {location ? (
            <PharmacyMap pharmacies={heroMapPharmacies} center={location} />
          ) : (
            <div className="home-hero__map-placeholder">
              <span>Standort festlegen, um Apotheken auf der Karte zu sehen.</span>
            </div>
          )}
        </div>
      </div>

      <div className="home-page__tiles">
        {quickLinks.map((link) => (
          <QuickLinkTile key={link.to} to={link.to} icon={link.icon} label={link.label} />
        ))}
      </div>

      <NearbyPharmaciesRow />

      <PopularMedicationsRow />
    </section>
  );
}
