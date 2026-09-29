import { useState } from "react";
import { ApiError } from "../../api/client";
import { useCreateAccountMutation, useIdAustriaLoginMutation, type ECardDetails } from "../../api/registration";
import { Icon } from "../../layout/Icon";

export interface IdAustriaLoginResult {
  fullName: string;
  dateOfBirth: string;
}

type Mode = "login" | "register";

export function IdAustriaLoginStep({
  onLoginSuccess,
  onAccountCreated,
}: {
  onLoginSuccess: (result: IdAustriaLoginResult) => void;
  onAccountCreated: (details: ECardDetails) => void;
}) {
  const [mode, setMode] = useState<Mode>("login");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [dateOfBirth, setDateOfBirth] = useState("");
  const login = useIdAustriaLoginMutation();
  const createAccount = useCreateAccountMutation();

  const canSubmit = firstName.trim() !== "" && lastName.trim() !== "" && dateOfBirth !== "";
  const isPending = login.isPending || createAccount.isPending;
  const isConflict =
    createAccount.isError && createAccount.error instanceof ApiError && createAccount.error.status === 409;

  const switchMode = (next: Mode) => {
    setMode(next);
    login.reset();
    createAccount.reset();
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSubmit) return;
    const fullName = `${firstName.trim()} ${lastName.trim()}`;

    if (mode === "login") {
      login.mutate(
        { fullName, dateOfBirth },
        { onSuccess: (identity) => onLoginSuccess({ fullName: identity.fullName, dateOfBirth: identity.dateOfBirth }) },
      );
    } else {
      createAccount.mutate({ fullName, dateOfBirth }, { onSuccess: onAccountCreated });
    }
  };

  return (
    <form className="registration-step" onSubmit={handleSubmit}>
      <div className="registration-step__icon">
        <Icon name="shield" size={32} />
      </div>

      <div className="registration-step__mode-toggle" role="tablist" aria-label="Anmelden oder registrieren">
        <button
          type="button"
          role="tab"
          aria-selected={mode === "login"}
          className={mode === "login" ? "is-active" : ""}
          onClick={() => switchMode("login")}
        >
          Anmelden
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={mode === "register"}
          className={mode === "register" ? "is-active" : ""}
          onClick={() => switchMode("register")}
        >
          Registrieren
        </button>
      </div>

      <h1>{mode === "login" ? "Anmeldung mit ID Austria" : "Neues Konto registrieren"}</h1>
      <p className="registration-step__intro">
        {mode === "login"
          ? "Melden Sie sich mit ID Austria an, um MedTrack mit Ihrer e-card zu verknüpfen. Diese Anmeldung ist eine Demo-Simulation - es besteht keine echte Verbindung zu ID Austria."
          : "Erstellen Sie ein neues MedTrack-Konto mit ID Austria. Sie erhalten sofort eine eigene SVNR, e-card und MedTrack-ID. Diese Registrierung ist eine Demo-Simulation - es besteht keine echte Verbindung zu ID Austria."}
      </p>

      <label>
        Vorname
        <input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
      </label>
      <label>
        Nachname
        <input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
      </label>
      <label>
        Geburtsdatum
        <input type="date" value={dateOfBirth} onChange={(e) => setDateOfBirth(e.target.value)} required />
      </label>

      {mode === "login" && login.isError && (
        <p className="registration-step__error">Anmeldung fehlgeschlagen. Bitte erneut versuchen.</p>
      )}
      {mode === "register" && createAccount.isError && (
        <p className="registration-step__error">
          {isConflict
            ? 'Ein Konto mit diesem Namen existiert bereits. Wechseln Sie zu "Anmelden".'
            : "Registrierung fehlgeschlagen. Bitte erneut versuchen."}
        </p>
      )}

      <button type="submit" className="registration-step__cta" disabled={!canSubmit || isPending}>
        {isPending
          ? mode === "login"
            ? "Anmelden…"
            : "Konto wird erstellt…"
          : mode === "login"
            ? "Mit ID Austria anmelden (Demo)"
            : "Konto erstellen (Demo)"}
      </button>
    </form>
  );
}
