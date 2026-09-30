import { createContext, useContext, useState, type ReactNode } from "react";

const REGISTERED_KEY = "medtrack.registrationComplete";
const LINKED_KEY = "medtrack.identityLinked";

function readStored(key: string): boolean {
  try {
    return localStorage.getItem(key) === "true";
  } catch {
    return false;
  }
}

function writeStored(key: string, value: boolean) {
  try {
    if (value) {
      localStorage.setItem(key, "true");
    } else {
      localStorage.removeItem(key);
    }
  } catch {
    // ignore storage failures (e.g. private browsing) - still holds for this session
  }
}

interface RegistrationContextValue {
  isRegistered: boolean;
  isLinked: boolean;
  completeRegistration: (linked: boolean) => void;
}

const RegistrationContext = createContext<RegistrationContextValue | undefined>(undefined);

// A real context (not a plain hook) so every consumer - App.tsx, HomePage's welcome text,
// PatientSelector's readonly state - sees the same isRegistered/isLinked at once. A plain hook here
// would give each caller its own independent useState copy, initialized once from localStorage at
// mount and never re-synced when another component calls completeRegistration().
export function RegistrationProvider({ children }: { children: ReactNode }) {
  const [isRegistered, setIsRegistered] = useState(() => readStored(REGISTERED_KEY));
  const [isLinked, setIsLinked] = useState(() => readStored(LINKED_KEY));

  const completeRegistration = (linked: boolean) => {
    writeStored(REGISTERED_KEY, true);
    setIsRegistered(true);
    if (linked) {
      writeStored(LINKED_KEY, true);
      setIsLinked(true);
    }
  };

  return (
    <RegistrationContext.Provider value={{ isRegistered, isLinked, completeRegistration }}>
      {children}
    </RegistrationContext.Provider>
  );
}

export function useRegistration(): RegistrationContextValue {
  const ctx = useContext(RegistrationContext);
  if (!ctx) {
    throw new Error("useRegistration must be used within a RegistrationProvider");
  }
  return ctx;
}
