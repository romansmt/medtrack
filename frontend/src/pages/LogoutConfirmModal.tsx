import { useState } from "react";
import { Icon } from "../layout/Icon";
import "./AuthModal.css";

type Step = "ask" | "confirm";

// Two separate, explicit confirmations before actually logging out, per the user's request - not a
// single dialog with a "sure?" checkbox. Reuses AuthModal's backdrop/card/registration-step styling.
export function LogoutConfirmModal({ onConfirm, onCancel }: { onConfirm: () => void; onCancel: () => void }) {
  const [step, setStep] = useState<Step>("ask");

  return (
    <div className="auth-modal-backdrop">
      <div className="auth-modal" role="dialog" aria-modal="true" aria-label="Abmelden">
        <div className="auth-modal__topbar">
          <span className="auth-modal__logo">MedTrack</span>
          <button type="button" className="auth-modal__close" onClick={onCancel} aria-label="Abbrechen">
            ×
          </button>
        </div>

        <div className="registration-step">
          <div className="registration-step__icon">
            <Icon name="logout" size={32} />
          </div>

          {step === "ask" ? (
            <>
              <h1>Abmelden?</h1>
              <p className="registration-step__intro">
                Möchten Sie sich wirklich abmelden? Sie können sich jederzeit erneut anmelden oder
                registrieren.
              </p>
              <button type="button" className="registration-step__cta" onClick={() => setStep("confirm")}>
                Ja, abmelden
              </button>
              <button type="button" className="registration-step__back" onClick={onCancel}>
                Abbrechen
              </button>
            </>
          ) : (
            <>
              <h1>Letzte Bestätigung</h1>
              <p className="registration-step__intro">
                Ihre Anmeldung wird vollständig zurückgesetzt - Sie beginnen wieder als nicht
                angemeldeter Gast, bis Sie sich neu anmelden oder registrieren. Fortfahren?
              </p>
              <button type="button" className="registration-step__cta" onClick={onConfirm}>
                Ja, jetzt abmelden
              </button>
              <button type="button" className="registration-step__back" onClick={onCancel}>
                Abbrechen
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
