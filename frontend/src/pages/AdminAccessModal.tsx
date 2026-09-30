import { useState } from "react";
import { useVerifyAdminCodeMutation } from "../api/admin";
import { Icon } from "../layout/Icon";
import "./AuthModal.css";

// Small standalone overlay for entering the admin access code (see docs/DEVELOPMENT.md). Reuses
// AuthModal's backdrop/card/registration-step styling rather than duplicating it - this is
// deliberately a single-field, single-purpose form, not another step inside AuthModal itself, since
// it has nothing to do with logging in as a patient.
export function AdminAccessModal({ onSuccess, onCancel }: { onSuccess: () => void; onCancel: () => void }) {
  const [code, setCode] = useState("");
  const verify = useVerifyAdminCodeMutation();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (code.trim() === "") return;
    verify.mutate(code.trim(), { onSuccess });
  };

  return (
    <div className="auth-modal-backdrop">
      <div className="auth-modal" role="dialog" aria-modal="true" aria-label="Admin-Zugang">
        <div className="auth-modal__topbar">
          <span className="auth-modal__logo">MedTrack</span>
          <button type="button" className="auth-modal__close" onClick={onCancel} aria-label="Abbrechen">
            ×
          </button>
        </div>

        <form className="registration-step" onSubmit={handleSubmit}>
          <div className="registration-step__icon">
            <Icon name="shield" size={32} />
          </div>
          <h1>Admin-Zugang</h1>
          <p className="registration-step__intro">
            Mit dem Admin-Zugangscode können Sie im Menü zwischen allen Patient:innen wechseln - diese
            Möglichkeit ist sonst nirgends sichtbar. Nur für Test-/Demo-Zwecke gedacht.
          </p>

          <label>
            Zugangscode
            <input
              type="password"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              autoFocus
              required
            />
          </label>

          {verify.isError && <p className="registration-step__error">Ungültiger Zugangscode.</p>}

          <button type="submit" className="registration-step__cta" disabled={code.trim() === "" || verify.isPending}>
            {verify.isPending ? "Prüfe…" : "Bestätigen"}
          </button>
        </form>
      </div>
    </div>
  );
}
