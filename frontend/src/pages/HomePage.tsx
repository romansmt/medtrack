import { LocationPicker } from "../components/LocationPicker";
import { NextOpenPharmacyCard } from "../components/NextOpenPharmacyCard";
import { QuickLinkTile } from "../components/QuickLinkTile";
import { SearchBar } from "../components/SearchBar";
import "./HomePage.css";

const quickLinks = [
  { to: "/pharmacies", icon: "store", label: "Geöffnete Apotheken in der Nähe" },
  { to: "/search", icon: "search", label: "Verfügbarkeit von Medikamenten" },
  { to: "/medication-plan", icon: "pill", label: "Mein Einnahmeplan" },
  { to: "/favorites", icon: "heart", label: "Meine Lieblingsapotheken" },
] as const;

export function HomePage() {
  return (
    <section className="home-page">
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
