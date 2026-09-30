import { LocationPicker } from "../components/LocationPicker";
import { NextOpenPharmacyCard } from "../components/NextOpenPharmacyCard";
import { QuickLinkTile } from "../components/QuickLinkTile";
import { SearchBar } from "../components/SearchBar";
import { useAdmin } from "../context/AdminContext";
import { usePatient } from "../context/PatientContext";
import { useRegistration } from "../context/RegistrationContext";
import "./HomePage.css";

const quickLinks = [
  { to: "/pharmacies", icon: "store", label: "Geöffnete Apotheken in der Nähe" },
  { to: "/search", icon: "search", label: "Verfügbarkeit von Medikamenten" },
  { to: "/medication-plan", icon: "pill", label: "Mein Einnahmeplan" },
  { to: "/favorites", icon: "heart", label: "Meine Lieblingsapotheken" },
] as const;

export function HomePage() {
  const { selectedPatient } = usePatient();
  const { isRegistered } = useRegistration();
  const { isAdmin } = useAdmin();

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

      <div className="home-page__hero">
        <h1>Österreichs Apotheken. Immer für Sie da!</h1>
        <p>Hier finden Sie Apotheken in Ihrer Nähe, aktuelle Öffnungszeiten und verfügbare Medikamente.</p>
      </div>

      <SearchBar />

      <LocationPicker />

      <div className="home-page__tiles">
        {quickLinks.map((link) => (
          <QuickLinkTile key={link.to} to={link.to} icon={link.icon} label={link.label} />
        ))}
      </div>

      <NextOpenPharmacyCard />
    </section>
  );
}
