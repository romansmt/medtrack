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

  useEffect(() => {
    if (patients.length === 0) return;
    const stillExists = patients.some((p) => p.svnr === selectedSvnr);
    if (!stillExists) {
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
