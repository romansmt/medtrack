import { usePatient } from "../context/PatientContext";

export function PatientSelector() {
  const { patients, selectedPatient, isLoading, error, selectPatient } = usePatient();

  if (isLoading) {
    return <span className="patient-selector patient-selector--status">Lade Patienten…</span>;
  }

  if (error) {
    return <span className="patient-selector patient-selector--status">Patienten nicht verfügbar</span>;
  }

  return (
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
  );
}
