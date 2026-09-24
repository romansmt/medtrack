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

export function useRegistration() {
  const [isRegistered, setIsRegistered] = useState(() => readStored(REGISTERED_KEY));
  // isLinked is only ever set true by actually completing the ID Austria + e-card flow, never by
  // skipping - it's what the "not linked yet" banner checks, independent of isRegistered.
  const [isLinked, setIsLinked] = useState(() => readStored(LINKED_KEY));

  const completeRegistration = (linked: boolean) => {
    writeStored(REGISTERED_KEY, true);
    setIsRegistered(true);
    if (linked) {
      writeStored(LINKED_KEY, true);
      setIsLinked(true);
    }
  };

  // Lets someone who skipped (or wants to re-verify) come back to the registration flow later -
  // only clears the "gate passed" flag, not isLinked, so a previously-linked identity isn't lost
  // unless they actually complete the flow again.
  const reopenRegistration = () => {
    writeStored(REGISTERED_KEY, false);
    setIsRegistered(false);
  };

  return { isRegistered, isLinked, completeRegistration, reopenRegistration };
}
