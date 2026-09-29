import { useState } from "react";
import { Route, Routes } from "react-router-dom";
import { AppShell } from "./layout/AppShell";
import { useConsent } from "./hooks/useConsent";
import { useRegistration } from "./hooks/useRegistration";
import { AuthModal } from "./pages/AuthModal";
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
  const { isRegistered, isLinked, completeRegistration } = useRegistration();
  // Whether the auth modal is visible right now - separate from isRegistered (whether it has ever
  // been passed). Initialized once from isRegistered so it opens automatically on a first-ever
  // visit, but afterwards it's just local UI state: AppShell's "ID Austria verknüpfen" banner
  // button can reopen it anytime without touching persisted registration state at all.
  const [authModalOpen, setAuthModalOpen] = useState(() => !isRegistered);

  if (!hasConsented) {
    return <ConsentPage onAccept={giveConsent} />;
  }

  return (
    <>
      <Routes>
        <Route element={<AppShell isLinked={isLinked} onOpenAuthModal={() => setAuthModalOpen(true)} />}>
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

      {authModalOpen && (
        <AuthModal
          onComplete={(linked) => {
            completeRegistration(linked);
            setAuthModalOpen(false);
          }}
        />
      )}
    </>
  );
}
