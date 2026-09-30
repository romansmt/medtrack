import { useState } from "react";
import { Route, Routes } from "react-router-dom";
import { AppShell } from "./layout/AppShell";
import { useAdmin } from "./hooks/useAdmin";
import { useConsent } from "./hooks/useConsent";
import { useRegistration } from "./hooks/useRegistration";
import { AccountSheet } from "./pages/AccountSheet";
import { AdminAccessModal } from "./pages/AdminAccessModal";
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
  const { isAdmin, setIsAdmin } = useAdmin();
  // All three start closed - none is ever forced open. Reached only via the header's account
  // button, the "not linked" banner, or a deliberate tap inside the account sheet.
  const [accountSheetOpen, setAccountSheetOpen] = useState(false);
  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [adminAccessModalOpen, setAdminAccessModalOpen] = useState(false);

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
              isAdmin={isAdmin}
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
          isAdmin={isAdmin}
          onOpenAuth={() => {
            setAccountSheetOpen(false);
            setAuthModalOpen(true);
          }}
          onOpenAdminAccess={() => {
            setAccountSheetOpen(false);
            setAdminAccessModalOpen(true);
          }}
          onDeactivateAdmin={() => setIsAdmin(false)}
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

      {adminAccessModalOpen && (
        <AdminAccessModal
          onSuccess={() => {
            setIsAdmin(true);
            setAdminAccessModalOpen(false);
          }}
          onCancel={() => setAdminAccessModalOpen(false)}
        />
      )}
    </>
  );
}
