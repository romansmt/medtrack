import { useState } from "react";
import { useIdAustriaLoginMutation } from "../../api/registration";
import { Icon } from "../../layout/Icon";

export interface IdAustriaLoginResult {
  fullName: string;
  dateOfBirth: string;
}

export function IdAustriaLoginStep({ onSuccess }: { onSuccess: (result: IdAustriaLoginResult) => void }) {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [dateOfBirth, setDateOfBirth] = useState("");
  const login = useIdAustriaLoginMutation();

  const canSubmit = firstName.trim() !== "" && lastName.trim() !== "" && dateOfBirth !== "";

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSubmit) return;
    const fullName = `${firstName.trim()} ${lastName.trim()}`;
    login.mutate(
      { fullName, dateOfBirth },
      { onSuccess: (identity) => onSuccess({ fullName: identity.fullName, dateOfBirth: identity.dateOfBirth }) },
    );
  };

  return (
    <form className="registration-step" onSubmit={handleSubmit}>
      <div className="registration-step__icon">
        <Icon name="shield" size={32} />
      </div>
      <h1>Anmeldung mit ID Austria</h1>
      <p className="registration-step__intro">
        Registrieren Sie sich einmalig mit ID Austria, um MedTrack mit Ihrer e-card zu verknüpfen.
        Diese Anmeldung ist eine Demo-Simulation - es besteht keine echte Verbindung zu ID Austria.
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

      {login.isError && <p className="registration-step__error">Anmeldung fehlgeschlagen. Bitte erneut versuchen.</p>}

      <button type="submit" className="registration-step__cta" disabled={!canSubmit || login.isPending}>
        {login.isPending ? "Anmelden…" : "Mit ID Austria anmelden (Demo)"}
      </button>
    </form>
  );
}
