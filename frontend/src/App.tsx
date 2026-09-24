import { Route, Routes } from "react-router-dom";
import { AppShell } from "./layout/AppShell";
import { useConsent } from "./hooks/useConsent";
import { ConsentPage } from "./pages/ConsentPage";
import { FavoritesPage } from "./pages/FavoritesPage";
import { HomePage } from "./pages/HomePage";
import { MedicationPlanPage } from "./pages/MedicationPlanPage";
import { PharmaciesPage } from "./pages/PharmaciesPage";
import { PrescriptionsPage } from "./pages/PrescriptionsPage";
import { PriceComparisonPage } from "./pages/PriceComparisonPage";
import { ReservationsPage } from "./pages/ReservationsPage";
import { SearchPage } from "./pages/SearchPage";

export function App() {
  const { hasConsented, giveConsent } = useConsent();

  if (!hasConsented) {
    return <ConsentPage onAccept={giveConsent} />;
  }

  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/pharmacies" element={<PharmaciesPage />} />
        <Route path="/drugs/:drugId/compare" element={<PriceComparisonPage />} />
        <Route path="/medication-plan" element={<MedicationPlanPage />} />
        <Route path="/favorites" element={<FavoritesPage />} />
        <Route path="/reservations" element={<ReservationsPage />} />
        <Route path="/prescriptions" element={<PrescriptionsPage />} />
      </Route>
    </Routes>
  );
}
