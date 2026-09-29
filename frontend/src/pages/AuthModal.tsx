import { useState } from "react";
import type { ECardDetails } from "../api/registration";
import { usePatient } from "../context/PatientContext";
import { ConfirmCardDataStep } from "./registration/ConfirmCardDataStep";
import { ECardScanStep } from "./registration/ECardScanStep";
import { IdAustriaLoginStep, type IdAustriaLoginResult } from "./registration/IdAustriaLoginStep";
import "./AuthModal.css";

type Step = "start" | "scan" | "confirm";

// The pseudo-ID-Austria login/registration overlay. Sits on top of the app (see App.tsx), not in
// place of it - closing it (skip, or completing either flow) just removes this component, no
// navigation involved.
export function AuthModal({ onComplete }: { onComplete: (linked: boolean) => void }) {
  const { selectPatient } = usePatient();
  const [step, setStep] = useState<Step>("start");
  const [identity, setIdentity] = useState<IdAustriaLoginResult | null>(null);
  const [cardDetails, setCardDetails] = useState<ECardDetails | null>(null);
  // "register" skips the scan step entirely (a freshly created account has nothing to photograph),
  // so confirm's "Zurück" needs to know which step to return to - "start", not "scan".
  const [cameFromRegister, setCameFromRegister] = useState(false);

  return (
    <div className="auth-modal-backdrop">
      <div className="auth-modal" role="dialog" aria-modal="true" aria-label="ID Austria Anmeldung">
        <div className="auth-modal__topbar">
          <span className="auth-modal__logo">MedTrack</span>
          <button
            type="button"
            className="auth-modal__close"
            onClick={() => onComplete(false)}
            aria-label="Schließen und ohne Anmeldung fortfahren"
          >
            ×
          </button>
        </div>
        <p className="auth-modal__skip-hint">
          Sie können diesen Dialog schließen und MedTrack ohne Verknüpfung Ihrer Daten nutzen -
          wählen Sie dazu später einen Demo-Patienten über das Menü oben. Sie können ID Austria
          jederzeit über den Hinweis im Menü erneut öffnen.
        </p>
        <div className="auth-modal__dots">
          {(["start", "scan", "confirm"] as Step[]).map((s) => (
            <span key={s} className={"auth-modal__dot" + (s === step ? " is-active" : "")} />
          ))}
        </div>

        {step === "start" && (
          <IdAustriaLoginStep
            onLoginSuccess={(result) => {
              setIdentity(result);
              setCameFromRegister(false);
              setStep("scan");
            }}
            onAccountCreated={(details) => {
              setIdentity({ fullName: `${details.firstName} ${details.lastName}`, dateOfBirth: details.dateOfBirth });
              setCardDetails(details);
              setCameFromRegister(true);
              setStep("confirm");
            }}
          />
        )}

        {step === "scan" && identity && (
          <ECardScanStep
            fullName={identity.fullName}
            onBack={() => setStep("start")}
            onSuccess={(details) => {
              setCardDetails(details);
              setStep("confirm");
            }}
            onManualEntry={() => {
              const [firstName, ...rest] = identity.fullName.split(" ");
              setCardDetails({
                svnr: "",
                medtrackId: "",
                firstName: firstName ?? "",
                lastName: rest.join(" "),
                dateOfBirth: identity.dateOfBirth,
                cardSerialNumber: "",
                carrierNumber: "",
                carrierName: "ÖGK",
                expiryDate: "",
              });
              setStep("confirm");
            }}
          />
        )}

        {step === "confirm" && identity && cardDetails && (
          <ConfirmCardDataStep
            details={cardDetails}
            registeredDateOfBirth={identity.dateOfBirth}
            onBack={() => setStep(cameFromRegister ? "start" : "scan")}
            onConfirm={(svnr) => {
              selectPatient(svnr);
              onComplete(true);
            }}
          />
        )}
      </div>
    </div>
  );
}
