import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { usePatientsQuery, type Patient } from "../api/patients";

const STORAGE_KEY = "medtrack.selectedPatientSvnr";

interface PatientContextValue {
  patients: Patient[];
  selectedPatient: Patient | undefined;
  isLoading: boolean;
  error: unknown;
  selectPatient: (svnr: string) => void;
}

const PatientContext = createContext<PatientContextValue | undefined>(undefined);

function readStoredSvnr(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

function writeStoredSvnr(svnr: string) {
  try {
    localStorage.setItem(STORAGE_KEY, svnr);
  } catch {
    // ignore storage failures (e.g. private browsing)
  }
}

export function PatientProvider({ children }: { children: ReactNode }) {
  const { data, isLoading, error } = usePatientsQuery();
  const patients = data ?? [];
  const [selectedSvnr, setSelectedSvnr] = useState<string | null>(readStoredSvnr);

  // Only fills in a default when nothing has ever been selected. Deliberately does NOT "correct" a
  // non-null selection that's briefly missing from `patients` - that's not a stale/invalid pointer,
  // it's a normal race after registering: selectPatient(newSvnr) fires in the same tick as the
  // create-account mutation's own query invalidation, so this effect can run again before the
  // refetch (which will include the new patient) resolves. Patients are never removed in this app,
  // so a real "selection no longer exists" case can't otherwise happen.
  useEffect(() => {
    if (patients.length === 0) return;
    if (selectedSvnr === null) {
      setSelectedSvnr(patients[0].svnr);
    }
  }, [patients, selectedSvnr]);

  const selectPatient = (svnr: string) => {
    setSelectedSvnr(svnr);
    writeStoredSvnr(svnr);
  };

  const selectedPatient = useMemo(
    () => patients.find((p) => p.svnr === selectedSvnr),
    [patients, selectedSvnr],
  );

  return (
    <PatientContext.Provider value={{ patients, selectedPatient, isLoading, error, selectPatient }}>
      {children}
    </PatientContext.Provider>
  );
}

export function usePatient(): PatientContextValue {
  const ctx = useContext(PatientContext);
  if (!ctx) {
    throw new Error("usePatient must be used within a PatientProvider");
  }
  return ctx;
}
