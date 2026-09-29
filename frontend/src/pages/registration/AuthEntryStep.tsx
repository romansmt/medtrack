import { useState } from "react";
import { ApiError } from "../../api/client";
import {
  useCreateAccountMutation,
  useIdAustriaLoginMutation,
  useStandardLoginMutation,
  type ECardDetails,
} from "../../api/registration";
import { Icon } from "../../layout/Icon";

export interface IdAustriaLoginResult {
  fullName: string;
  dateOfBirth: string;
}

type OuterMode = "login" | "register";
type Method = "idAustria" | "standard";

// The modal's first screen: two independent choices layered on top of each other - Anmelden vs.
// Registrieren (outer tabs), and Mit ID Austria vs. Standard (inner tabs). ID Austria always leads
// to a follow-up step (e-card scan for login, e-card confirm for register - see AuthModal); Standard
// never does, since there's no e-card to double-check, so both its login and register success cases
// share one callback.
export function AuthEntryStep({
  onIdAustriaLoginSuccess,
  onIdAustriaAccountCreated,
  onStandardAuthSuccess,
}: {
  onIdAustriaLoginSuccess: (result: IdAustriaLoginResult) => void;
  onIdAustriaAccountCreated: (details: ECardDetails) => void;
  onStandardAuthSuccess: (details: ECardDetails) => void;
}) {
  const [outerMode, setOuterMode] = useState<OuterMode>("login");
  const [method, setMethod] = useState<Method>("idAustria");

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [dateOfBirth, setDateOfBirth] = useState("");

  const [stdFirstName, setStdFirstName] = useState("");
  const [stdLastName, setStdLastName] = useState("");
  const [stdDateOfBirth, setStdDateOfBirth] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [agbAccepted, setAgbAccepted] = useState(false);

  const idLogin = useIdAustriaLoginMutation();
  const createAccount = useCreateAccountMutation();
  const standardLogin = useStandardLoginMutation();

  const isNameConflict =
    createAccount.isError && createAccount.error instanceof ApiError && createAccount.error.status === 409;
  const standardLoginNotFound =
    standardLogin.isError && standardLogin.error instanceof ApiError && standardLogin.error.status === 404;

  const resetMutations = () => {
    idLogin.reset();
    createAccount.reset();
    standardLogin.reset();
  };

  const switchOuterMode = (next: OuterMode) => {
    setOuterMode(next);
    resetMutations();
  };
  const switchMethod = (next: Method) => {
    setMethod(next);
    resetMutations();
  };

  const idAustriaCanSubmit = firstName.trim() !== "" && lastName.trim() !== "" && dateOfBirth !== "";
  const standardRegisterCanSubmit =
    stdFirstName.trim() !== "" &&
    stdLastName.trim() !== "" &&
    stdDateOfBirth !== "" &&
    email.trim() !== "" &&
    password !== "" &&
    password === passwordConfirm &&
    agbAccepted;
  const standardLoginCanSubmit = email.trim() !== "" && password !== "";

  const isPending = idLogin.isPending || createAccount.isPending || standardLogin.isPending;

  const handleIdAustriaSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!idAustriaCanSubmit) return;
    const fullName = `${firstName.trim()} ${lastName.trim()}`;

    if (outerMode === "login") {
      idLogin.mutate(
        { fullName, dateOfBirth },
        {
          onSuccess: (identity) =>
            onIdAustriaLoginSuccess({ fullName: identity.fullName, dateOfBirth: identity.dateOfBirth }),
        },
      );
    } else {
      createAccount.mutate({ fullName, dateOfBirth }, { onSuccess: onIdAustriaAccountCreated });
    }
  };

  const handleStandardSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (outerMode === "login") {
      if (!standardLoginCanSubmit) return;
      standardLogin.mutate(email.trim(), { onSuccess: onStandardAuthSuccess });
    } else {
      if (!standardRegisterCanSubmit) return;
      const fullName = `${stdFirstName.trim()} ${stdLastName.trim()}`;
      createAccount.mutate(
        { fullName, dateOfBirth: stdDateOfBirth, email: email.trim() },
        { onSuccess: onStandardAuthSuccess },
      );
    }
  };

  return (
    <div className="registration-step">
      <div className="registration-step__icon">
        <Icon name={method === "idAustria" ? "shield" : "user"} size={32} />
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

      <div className="registration-step__method-toggle" role="tablist" aria-label="Anmeldemethode">
        <button
          type="button"
          role="tab"
          aria-selected={method === "idAustria"}
          className={method === "idAustria" ? "is-active" : ""}
          onClick={() => switchMethod("idAustria")}
        >
          Mit ID Austria
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={method === "standard"}
          className={method === "standard" ? "is-active" : ""}
          onClick={() => switchMethod("standard")}
        >
          Standard
        </button>
      </div>

      {method === "idAustria" ? (
        <form className="registration-step" onSubmit={handleIdAustriaSubmit}>
          <h1>{outerMode === "login" ? "Anmeldung mit ID Austria" : "Neues Konto registrieren"}</h1>
          <p className="registration-step__intro">
            {outerMode === "login"
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

          {outerMode === "login" && idLogin.isError && (
            <p className="registration-step__error">Anmeldung fehlgeschlagen. Bitte erneut versuchen.</p>
          )}
          {outerMode === "register" && createAccount.isError && (
            <p className="registration-step__error">
              {isNameConflict
                ? 'Ein Konto mit diesem Namen existiert bereits. Wechseln Sie zu "Anmelden".'
                : "Registrierung fehlgeschlagen. Bitte erneut versuchen."}
            </p>
          )}

          <button type="submit" className="registration-step__cta" disabled={!idAustriaCanSubmit || isPending}>
            {isPending
              ? outerMode === "login"
                ? "Anmelden…"
                : "Konto wird erstellt…"
              : outerMode === "login"
                ? "Mit ID Austria anmelden (Demo)"
                : "Konto erstellen (Demo)"}
          </button>
        </form>
      ) : (
        <form className="registration-step" onSubmit={handleStandardSubmit}>
          <h1>{outerMode === "login" ? "Standard-Anmeldung" : "Standard-Registrierung"}</h1>
          <p className="registration-step__intro">
            {outerMode === "login"
              ? "Melden Sie sich mit E-Mail und Passwort an - ohne ID Austria. Sie können Ihr Konto später jederzeit über \"ID Austria verknüpfen\" verifizieren."
              : "Erstellen Sie ein MedTrack-Konto ganz ohne ID Austria, wie bei einer gewöhnlichen Registrierung. Das Geburtsdatum wird für Ihre Medikationsdaten benötigt."}
          </p>

          {outerMode === "register" && (
            <>
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
                Geburtsdatum
                <input
                  type="date"
                  value={stdDateOfBirth}
                  onChange={(e) => setStdDateOfBirth(e.target.value)}
                  required
                />
              </label>
            </>
          )}

          <label>
            E-Mail
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>

          {outerMode === "register" && (
            <label>
              Telefonnummer
              <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} required />
            </label>
          )}

          <label>
            Passwort
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </label>

          {outerMode === "register" && (
            <label>
              Passwort bestätigen
              <input
                type="password"
                value={passwordConfirm}
                onChange={(e) => setPasswordConfirm(e.target.value)}
                required
              />
            </label>
          )}

          {outerMode === "register" && password !== "" && passwordConfirm !== "" && password !== passwordConfirm && (
            <p className="registration-step__error">Die Passwörter stimmen nicht überein.</p>
          )}

          {outerMode === "register" && (
            <label className="registration-step__checkbox-row">
              <input type="checkbox" checked={agbAccepted} onChange={(e) => setAgbAccepted(e.target.checked)} />
              <span>Ich akzeptiere die AGB (Demo-Projekt - es gelten keine echten Nutzungsbedingungen)</span>
            </label>
          )}

          {outerMode === "login" && standardLogin.isError && (
            <p className="registration-step__error">
              {standardLoginNotFound
                ? 'Kein Konto mit dieser E-Mail gefunden. Wechseln Sie zu "Registrieren".'
                : "Anmeldung fehlgeschlagen. Bitte erneut versuchen."}
            </p>
          )}
          {outerMode === "register" && createAccount.isError && (
            <p className="registration-step__error">
              {isNameConflict
                ? 'Ein Konto mit diesem Namen existiert bereits. Wechseln Sie zu "Anmelden".'
                : "Registrierung fehlgeschlagen. Bitte erneut versuchen."}
            </p>
          )}
          <p className="registration-step__hint">
            Demo-Hinweis: Das Passwort wird nur clientseitig geprüft (Bestätigung stimmt überein) und
            nirgends gespeichert - bei der Standard-Anmeldung genügt daher jede E-Mail, die bereits
            registriert wurde.
          </p>

          <button
            type="submit"
            className="registration-step__cta"
            disabled={isPending || (outerMode === "login" ? !standardLoginCanSubmit : !standardRegisterCanSubmit)}
          >
            {isPending
              ? outerMode === "login"
                ? "Anmelden…"
                : "Konto wird erstellt…"
              : outerMode === "login"
                ? "Anmelden"
                : "Registrieren"}
          </button>
        </form>
      )}
    </div>
  );
}
