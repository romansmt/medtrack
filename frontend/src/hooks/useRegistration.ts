import { useState } from "react";

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

// Tracks two independent, persisted facts. Whether the auth modal is currently *visible* is a
// separate, ephemeral concern owned by App.tsx (see authModalOpen there) - reopening the modal
// later doesn't need to touch either flag here until the user actually completes it again.
export function useRegistration() {
  const [isRegistered, setIsRegistered] = useState(() => readStored(REGISTERED_KEY));
  // isLinked is only ever set true by actually completing the ID Austria + e-card flow (login or
  // register), never by skipping - it's what the "not linked yet" banner checks.
  const [isLinked, setIsLinked] = useState(() => readStored(LINKED_KEY));

  const completeRegistration = (linked: boolean) => {
    writeStored(REGISTERED_KEY, true);
    setIsRegistered(true);
    if (linked) {
      writeStored(LINKED_KEY, true);
      setIsLinked(true);
    }
  };

  return { isRegistered, isLinked, completeRegistration };
}
