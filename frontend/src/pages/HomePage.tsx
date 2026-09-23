import { usePatient } from "../context/PatientContext";

export function HomePage() {
  const { selectedPatient, isLoading } = usePatient();

  return (
    <section>
      <h1>Home</h1>
      <p style={{ color: "var(--color-text-muted)" }}>Wird in TASK-28 umgesetzt.</p>
      <p>
        {isLoading
          ? "Lade Demo-Patienten…"
          : selectedPatient
            ? `Angemeldet als: ${selectedPatient.name} (${selectedPatient.svnr})`
            : "Kein Demo-Patient verfügbar."}
      </p>
    </section>
  );
}
