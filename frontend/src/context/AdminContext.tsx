import { createContext, useContext, useState, type ReactNode } from "react";

const ADMIN_KEY = "medtrack.isAdmin";

function readStored(): boolean {
  try {
    return localStorage.getItem(ADMIN_KEY) === "true";
  } catch {
    return false;
  }
}

function writeStored(value: boolean) {
  try {
    if (value) {
      localStorage.setItem(ADMIN_KEY, "true");
    } else {
      localStorage.removeItem(ADMIN_KEY);
    }
  } catch {
    // ignore storage failures (e.g. private browsing) - still holds for this session
  }
}

interface AdminContextValue {
  isAdmin: boolean;
  setIsAdmin: (value: boolean) => void;
}

const AdminContext = createContext<AdminContextValue | undefined>(undefined);

// Whether the patient switcher (and any other admin-only UI) is unlocked in this browser. Set only
// by successfully verifying the access code against POST /api/admin/verify-code (see
// AdminAccessModal) - there is no server-side session, this is purely a local convenience flag, same
// as isRegistered/isLinked in RegistrationContext. A real context, not a plain hook, for the same
// reason as RegistrationContext: every consumer needs to see the same value at once.
export function AdminProvider({ children }: { children: ReactNode }) {
  const [isAdmin, setIsAdminState] = useState(readStored);

  const setIsAdmin = (value: boolean) => {
    writeStored(value);
    setIsAdminState(value);
  };

  return <AdminContext.Provider value={{ isAdmin, setIsAdmin }}>{children}</AdminContext.Provider>;
}

export function useAdmin(): AdminContextValue {
  const ctx = useContext(AdminContext);
  if (!ctx) {
    throw new Error("useAdmin must be used within an AdminProvider");
  }
  return ctx;
}
