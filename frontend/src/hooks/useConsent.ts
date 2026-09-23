import { useState } from "react";

const STORAGE_KEY = "medtrack.consentGiven";

function readStoredConsent(): boolean {
  try {
    return localStorage.getItem(STORAGE_KEY) === "true";
  } catch {
    return false;
  }
}

export function useConsent() {
  const [hasConsented, setHasConsented] = useState(readStoredConsent);

  const giveConsent = () => {
    try {
      localStorage.setItem(STORAGE_KEY, "true");
    } catch {
      // ignore storage failures (e.g. private browsing) - consent still holds for this session
    }
    setHasConsented(true);
  };

  return { hasConsented, giveConsent };
}
