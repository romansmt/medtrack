import { useState } from "react";
import { ApiError } from "../../api/client";
import { useCompleteRegistrationMutation } from "../../api/registration";
import { Icon } from "../../layout/Icon";

// The final registration step, only reached once verify-identity has confirmed the submitted data
// matches ID Austria's record and no MedTrack account exists for it yet. E-Mail + Passwort set here
// become the credential used for Standard (non-ID-Austria) login afterward.
export function CreateCredentialsStep({
  svnr,
  onBack,
  onSuccess,
}: {
  svnr: string;
  onBack: () => void;
  onSuccess: (svnr: string) => void;
}) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const completeRegistration = useCompleteRegistrationMutation();

  const passwordsMatch = password !== "" && password === passwordConfirm;
  const canSubmit = email.trim() !== "" && passwordsMatch;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSubmit) return;
    completeRegistration.mutate(
      { svnr, email: email.trim(), password },
      { onSuccess: () => onSuccess(svnr) },
    );
  };

  return (
    <form className="registration-step" onSubmit={handleSubmit}>
      <div className="registration-step__icon">
        <Icon name="user" size={32} />
      </div>
      <h1>Konto abschließen</h1>
      <p className="registration-step__intro">
        Ihre Daten wurden erfolgreich mit dem ID-Austria-Register abgeglichen. Legen Sie zum Abschluss
        eine E-Mail-Adresse und ein Passwort fest - damit können Sie sich künftig auch ohne ID Austria
        anmelden.
      </p>

      <label>
        E-Mail
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
      </label>
      <label>
        Passwort
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
      </label>
      <label>
        Passwort bestätigen
        <input
          type="password"
          value={passwordConfirm}
          onChange={(e) => setPasswordConfirm(e.target.value)}
          required
        />
      </label>

      {password !== "" && passwordConfirm !== "" && !passwordsMatch && (
        <p className="registration-step__error">Die Passwörter stimmen nicht überein.</p>
      )}

      {completeRegistration.isError && (
        <p className="registration-step__error">
          {completeRegistration.error instanceof ApiError
            ? completeRegistration.error.message
            : "Konto konnte nicht erstellt werden. Bitte erneut versuchen."}
        </p>
      )}

      <button type="submit" className="registration-step__cta" disabled={!canSubmit || completeRegistration.isPending}>
        {completeRegistration.isPending ? "Konto wird erstellt…" : "Konto erstellen"}
      </button>
      <button type="button" className="registration-step__back" onClick={onBack}>
        Zurück
      </button>
    </form>
  );
}
