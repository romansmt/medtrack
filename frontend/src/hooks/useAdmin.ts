import { useState } from "react";

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

// Whether the patient switcher (and any other admin-only UI) is unlocked in this browser. Set only
// by successfully verifying the access code against POST /api/admin/verify-code (see
// AdminAccessModal) - there is no server-side session, this is purely a local convenience flag, same
// as isRegistered/isLinked in useRegistration.
export function useAdmin() {
  const [isAdmin, setIsAdminState] = useState(readStored);

  const setIsAdmin = (value: boolean) => {
    writeStored(value);
    setIsAdminState(value);
  };

  return { isAdmin, setIsAdmin };
}
