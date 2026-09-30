import { usePatient } from "../context/PatientContext";

// Switching between patients is an admin-only capability (see useAdmin/AdminAccessModal) - everyone
// else gets a plain read-only display of their own current name, with no dropdown and no way to see
// that other patients even exist.
export function PatientSelector({ isAdmin }: { isAdmin: boolean }) {
  const { patients, selectedPatient, isLoading, error, selectPatient } = usePatient();

  if (isLoading) {
    return <span className="patient-selector patient-selector--status">Lade Patienten…</span>;
  }

  if (error) {
    return <span className="patient-selector patient-selector--status">Patienten nicht verfügbar</span>;
  }

  if (!isAdmin) {
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
