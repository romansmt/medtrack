import { useRegistration } from "../context/RegistrationContext";
import { usePatient } from "../context/PatientContext";

// Switching between patients is an admin-only capability (see useAdmin/AdminAccessModal) - everyone
// else gets a plain read-only display of their own current name, with no dropdown and no way to see
// that other patients even exist. Before a real login/registration, PatientContext still silently
// defaults selectedPatient to the first seeded patient (so the demo pages have something to show) -
// but that default is never a real identity, so it's deliberately not shown here until isRegistered.
export function PatientSelector({ isAdmin }: { isAdmin: boolean }) {
  const { patients, selectedPatient, isLoading, error, selectPatient } = usePatient();
  const { isRegistered } = useRegistration();

  if (isLoading) {
    return <span className="patient-selector patient-selector--status">Lade Patienten…</span>;
  }

  if (error) {
    return <span className="patient-selector patient-selector--status">Patienten nicht verfügbar</span>;
  }

  if (!isAdmin) {
    if (!isRegistered) {
      return null;
    }
    return (
      <div className="patient-selector-wrap">
        <span className="patient-selector patient-selector--readonly">{selectedPatient?.name ?? ""}</span>
        {selectedPatient && <span className="patient-selector__id">{selectedPatient.medtrackId}</span>}
      </div>
    );
  }

  return (
    <div className="patient-selector-wrap">
      <select
        className="patient-selector"
        value={selectedPatient?.svnr ?? ""}
        onChange={(e) => selectPatient(e.target.value)}
        aria-label="Demo-Patient wählen"
      >
        {patients.map((patient) => (
          <option key={patient.svnr} value={patient.svnr}>
            {patient.name}
          </option>
        ))}
      </select>
      {selectedPatient && <span className="patient-selector__id">{selectedPatient.medtrackId}</span>}
    </div>
  );
}
