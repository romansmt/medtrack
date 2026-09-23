import { Route, Routes } from "react-router-dom";
import { AppShell } from "./layout/AppShell";
import { useConsent } from "./hooks/useConsent";
import { ConsentPage } from "./pages/ConsentPage";
import { HomePage } from "./pages/HomePage";
import { PlaceholderPage } from "./pages/PlaceholderPage";

export function App() {
  const { hasConsented, giveConsent } = useConsent();

  if (!hasConsented) {
    return <ConsentPage onAccept={giveConsent} />;
  }

  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/" element={<HomePage />} />
        <Route
          path="/search"
          element={<PlaceholderPage title="Suche & Verfügbarkeit" task="TASK-29" />}
        />
        <Route
          path="/pharmacies"
          element={<PlaceholderPage title="Apotheken" task="TASK-30" />}
        />
        <Route
          path="/medication-plan"
          element={<PlaceholderPage title="Einnahmeplan" task="TASK-32" />}
        />
        <Route
          path="/favorites"
          element={<PlaceholderPage title="Favoriten" task="TASK-33" />}
        />
        <Route
          path="/reservations"
          element={<PlaceholderPage title="Reservierungen" task="TASK-33" />}
        />
        <Route
          path="/prescriptions"
          element={<PlaceholderPage title="Meine Rezepte" task="TASK-34" />}
        />
      </Route>
    </Routes>
  );
}
