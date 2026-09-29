import { useState } from "react";
import { Route, Routes } from "react-router-dom";
import { AppShell } from "./layout/AppShell";
import { useConsent } from "./hooks/useConsent";
import { useRegistration } from "./hooks/useRegistration";
import { AccountSheet } from "./pages/AccountSheet";
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
  // Both start closed - neither is ever forced open. Reached only via the header's account button,
  // the "not linked" banner, or a deliberate "Anmelden" tap inside the account sheet.
  const [accountSheetOpen, setAccountSheetOpen] = useState(false);
  const [authModalOpen, setAuthModalOpen] = useState(false);

  if (!hasConsented) {
    return <ConsentPage onAccept={giveConsent} />;
  }

  return (
    <>
      <Routes>
        <Route
          element={
            <AppShell
              isLinked={isLinked}
              onOpenAuthModal={() => setAuthModalOpen(true)}
              onOpenAccount={() => setAccountSheetOpen(true)}
            />
          }
        >
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

      {accountSheetOpen && (
        <AccountSheet
          isRegistered={isRegistered}
          isLinked={isLinked}
          onOpenAuth={() => {
            setAccountSheetOpen(false);
            setAuthModalOpen(true);
          }}
          onClose={() => setAccountSheetOpen(false)}
        />
      )}

      {authModalOpen && (
        <AuthModal
          onSuccess={(linked) => {
            completeRegistration(linked);
            setAuthModalOpen(false);
          }}
          onCancel={() => setAuthModalOpen(false)}
        />
      )}
    </>
  );
}
