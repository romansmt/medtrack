import { useState } from "react";
import { ApiError } from "../../api/client";
import { useIdAustriaLoginMutation, useStandardLoginMutation } from "../../api/registration";
import { Icon } from "../../layout/Icon";

export interface IdAustriaLoginResult {
  fullName: string;
  dateOfBirth: string;
}

type OuterMode = "login" | "register";

// The modal's first screen: Anmelden vs. Registrieren, each with exactly one method - no inner
// toggle anymore. Registrieren is ID-Austria only (MedTrack accounts can only be created that way);
// it asserts a name+birthdate here and continues into the e-card scan/confirm/credentials steps in
// AuthModal. Anmelden is credential-only: Vorname, Nachname, E-Mail and Passwort, checked against the
// account created by that registration flow - no further steps, resolves right here.
export function AuthEntryStep({
  onIdAustriaSuccess,
  onStandardLoginSuccess,
}: {
  onIdAustriaSuccess: (result: IdAustriaLoginResult) => void;
  onStandardLoginSuccess: (svnr: string) => void;
}) {
  const [outerMode, setOuterMode] = useState<OuterMode>("login");

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [dateOfBirth, setDateOfBirth] = useState("");

  const [stdFirstName, setStdFirstName] = useState("");
  const [stdLastName, setStdLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const idLogin = useIdAustriaLoginMutation();
  const standardLogin = useStandardLoginMutation();

  const standardLoginNotFound =
    standardLogin.isError && standardLogin.error instanceof ApiError && standardLogin.error.status === 404;
  const standardLoginInvalid =
    standardLogin.isError && standardLogin.error instanceof ApiError && standardLogin.error.status === 401;

  const switchOuterMode = (next: OuterMode) => {
    setOuterMode(next);
    idLogin.reset();
    standardLogin.reset();
  };

  const idAustriaCanSubmit = firstName.trim() !== "" && lastName.trim() !== "" && dateOfBirth !== "";
  const standardLoginCanSubmit =
    stdFirstName.trim() !== "" && stdLastName.trim() !== "" && email.trim() !== "" && password !== "";

  const isPending = idLogin.isPending || standardLogin.isPending;

  const handleIdAustriaSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!idAustriaCanSubmit) return;
    const fullName = `${firstName.trim()} ${lastName.trim()}`;

    idLogin.mutate(
      { fullName, dateOfBirth },
      { onSuccess: (identity) => onIdAustriaSuccess({ fullName: identity.fullName, dateOfBirth: identity.dateOfBirth }) },
    );
  };

  const handleStandardSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!standardLoginCanSubmit) return;
    standardLogin.mutate(
      { firstName: stdFirstName.trim(), lastName: stdLastName.trim(), email: email.trim(), password },
      { onSuccess: (account) => onStandardLoginSuccess(account.svnr) },
    );
  };

  return (
    <div className="registration-step">
      <div className="registration-step__icon">
        <Icon name={outerMode === "register" ? "shield" : "user"} size={32} />
      </div>

      <div className="registration-step__mode-toggle" role="tablist" aria-label="Anmelden oder registrieren">
        <button
          type="button"
          role="tab"
          aria-selected={outerMode === "login"}
          className={outerMode === "login" ? "is-active" : ""}
          onClick={() => switchOuterMode("login")}
        >
          Anmelden
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={outerMode === "register"}
          className={outerMode === "register" ? "is-active" : ""}
          onClick={() => switchOuterMode("register")}
        >
          Registrieren
        </button>
      </div>

      {outerMode === "register" ? (
        <form className="registration-step" onSubmit={handleIdAustriaSubmit}>
          <h1>Neues Konto registrieren</h1>
          <p className="registration-step__intro">
            MedTrack-Konten können nur mit ID Austria erstellt werden. Im nächsten Schritt scannen Sie
            Ihre e-card - die Daten werden mit dem ID-Austria-Register abgeglichen, bevor Sie E-Mail und
            Passwort für die spätere Anmeldung festlegen. Diese Registrierung ist eine
            Demo-Simulation - es besteht keine echte Verbindung zu ID Austria.
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

          {idLogin.isError && (
            <p className="registration-step__error">Registrierung fehlgeschlagen. Bitte erneut versuchen.</p>
          )}

          <button type="submit" className="registration-step__cta" disabled={!idAustriaCanSubmit || isPending}>
            {isPending ? "Wird verarbeitet…" : "Weiter zur e-card-Prüfung (Demo)"}
          </button>
        </form>
      ) : (
        <form className="registration-step" onSubmit={handleStandardSubmit}>
          <h1>Anmeldung</h1>
          <p className="registration-step__intro">
            Melden Sie sich mit den Daten an, die Sie bei der ID-Austria-Registrierung festgelegt
            haben: Vorname, Nachname, E-Mail und Passwort.
          </p>

          <div className="registration-step__row">
            <label>
              Vorname
              <input type="text" value={stdFirstName} onChange={(e) => setStdFirstName(e.target.value)} required />
            </label>
            <label>
              Nachname
              <input type="text" value={stdLastName} onChange={(e) => setStdLastName(e.target.value)} required />
            </label>
          </div>
          <label>
            E-Mail
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label>
            Passwort
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </label>

          {standardLogin.isError && (
            <p className="registration-step__error">
              {standardLoginNotFound
                ? 'Kein Konto mit dieser E-Mail gefunden. Wechseln Sie zu "Registrieren", um zuerst eines anzulegen.'
                : standardLoginInvalid
                  ? "Name, E-Mail oder Passwort sind nicht korrekt."
                  : "Anmeldung fehlgeschlagen. Bitte erneut versuchen."}
            </p>
          )}

          <button type="submit" className="registration-step__cta" disabled={isPending || !standardLoginCanSubmit}>
            {isPending ? "Anmelden…" : "Anmelden"}
          </button>
        </form>
      )}
    </div>
  );
}
