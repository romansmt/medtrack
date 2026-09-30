import { useState } from "react";
import type { ECardDetails } from "../api/registration";
import { usePatient } from "../context/PatientContext";
import { ConfirmCardDataStep } from "./registration/ConfirmCardDataStep";
import { CreateCredentialsStep } from "./registration/CreateCredentialsStep";
import { ECardScanStep } from "./registration/ECardScanStep";
import { AuthEntryStep, type IdAustriaLoginResult } from "./registration/AuthEntryStep";
import "./AuthModal.css";

type Step = "start" | "scan" | "confirm" | "credentials";

// The Anmelden/Registrieren overlay. Opened deliberately from the header's account button or the
// "not linked" banner (see App.tsx) - never forced open. Sits on top of the app, not in place of it:
// routes keep rendering underneath, closing this just removes the component, no navigation involved.
export function AuthModal({ onSuccess, onCancel }: { onSuccess: (linked: boolean) => void; onCancel: () => void }) {
  const { selectPatient } = usePatient();
  const [step, setStep] = useState<Step>("start");
  const [identity, setIdentity] = useState<IdAustriaLoginResult | null>(null);
  const [cardDetails, setCardDetails] = useState<ECardDetails | null>(null);
  // Registration always goes through scan -> confirm -> credentials; login stops at confirm (either
  // logging straight in or telling the user to register first). "Zurück" on confirm needs to know
  // which step to return to.
  const [cameFromRegister, setCameFromRegister] = useState(false);
  const [verifiedSvnr, setVerifiedSvnr] = useState<string | null>(null);

  return (
    <div className="auth-modal-backdrop">
      <div className="auth-modal" role="dialog" aria-modal="true" aria-label="Anmelden oder registrieren">
        <div className="auth-modal__topbar">
          <span className="auth-modal__logo">MedTrack</span>
          <button type="button" className="auth-modal__close" onClick={onCancel} aria-label="Abbrechen">
            ×
          </button>
        </div>
        <div className="auth-modal__dots">
          {(["start", "scan", "confirm", "credentials"] as Step[]).map((s) => (
            <span key={s} className={"auth-modal__dot" + (s === step ? " is-active" : "")} />
          ))}
        </div>

        {step === "start" && (
          <AuthEntryStep
            onIdAustriaSuccess={(result, isRegister) => {
              setIdentity(result);
              setCameFromRegister(isRegister);
              setStep("scan");
            }}
            onStandardLoginSuccess={(svnr) => {
              selectPatient(svnr);
              onSuccess(false);
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
                medtrackId: null,
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
            intent={cameFromRegister ? "register" : "login"}
            onBack={() => setStep(cameFromRegister ? "start" : "scan")}
            onVerifiedForRegistration={(svnr) => {
              setVerifiedSvnr(svnr);
              setStep("credentials");
            }}
            onLoginSuccess={(svnr) => {
              selectPatient(svnr);
              onSuccess(true);
            }}
          />
        )}

        {step === "credentials" && verifiedSvnr && (
          <CreateCredentialsStep
            svnr={verifiedSvnr}
            onBack={() => setStep("confirm")}
            onSuccess={(svnr) => {
              selectPatient(svnr);
              onSuccess(true);
            }}
          />
        )}
      </div>
    </div>
  );
}
