import { useState } from "react";
import type { ECardDetails } from "../api/registration";
import { usePatient } from "../context/PatientContext";
import { ConfirmCardDataStep } from "./registration/ConfirmCardDataStep";
import { ECardScanStep } from "./registration/ECardScanStep";
import { IdAustriaLoginStep, type IdAustriaLoginResult } from "./registration/IdAustriaLoginStep";
import "./RegistrationPage.css";

type Step = "login" | "scan" | "confirm";

export function RegistrationPage({ onComplete }: { onComplete: () => void }) {
  const { selectPatient } = usePatient();
  const [step, setStep] = useState<Step>("login");
  const [identity, setIdentity] = useState<IdAustriaLoginResult | null>(null);
  const [cardDetails, setCardDetails] = useState<ECardDetails | null>(null);

  return (
    <div className="registration-page">
      <div className="registration-card">
        <div className="registration-topbar">
          <span className="registration-logo">MedTrack</span>
          <button type="button" className="registration-skip" onClick={onComplete}>
            Ohne Anmeldung fortfahren
          </button>
        </div>
        <p className="registration-skip-hint">
          Sie können die ID-Austria-Anmeldung und den e-card-Scan überspringen und MedTrack ohne
          Verknüpfung Ihrer Daten nutzen - wählen Sie dazu später einen Demo-Patienten über das Menü
          oben.
        </p>
        <div className="registration-dots">
          {(["login", "scan", "confirm"] as Step[]).map((s) => (
            <span key={s} className={"registration-dot" + (s === step ? " is-active" : "")} />
          ))}
        </div>

        {step === "login" && (
          <IdAustriaLoginStep
            onSuccess={(result) => {
              setIdentity(result);
              setStep("scan");
            }}
          />
        )}

        {step === "scan" && identity && (
          <ECardScanStep
            fullName={identity.fullName}
            onBack={() => setStep("login")}
            onSuccess={(details) => {
              setCardDetails(details);
              setStep("confirm");
            }}
          />
        )}

        {step === "confirm" && identity && cardDetails && (
          <ConfirmCardDataStep
            details={cardDetails}
            registeredDateOfBirth={identity.dateOfBirth}
            onBack={() => setStep("scan")}
            onConfirm={(svnr) => {
              selectPatient(svnr);
              onComplete();
            }}
          />
        )}
      </div>
    </div>
  );
}
