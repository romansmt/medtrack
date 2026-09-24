import { useState } from "react";

const STORAGE_KEY = "medtrack.registrationComplete";

function readStoredRegistration(): boolean {
  try {
    return localStorage.getItem(STORAGE_KEY) === "true";
  } catch {
    return false;
  }
}

export function useRegistration() {
  const [isRegistered, setIsRegistered] = useState(readStoredRegistration);

  const completeRegistration = () => {
    try {
      localStorage.setItem(STORAGE_KEY, "true");
    } catch {
      // ignore storage failures (e.g. private browsing) - registration still holds for this session
    }
    setIsRegistered(true);
  };

  return { isRegistered, completeRegistration };
}
