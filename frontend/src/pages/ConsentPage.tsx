import { useState } from "react";
import { Icon } from "../layout/Icon";
import { consentSlide, introSlides } from "./onboardingSlides";
import "./ConsentPage.css";

const slides = [...introSlides, consentSlide];

export function ConsentPage({ onAccept }: { onAccept: () => void }) {
  const [step, setStep] = useState(0);
  const [checked, setChecked] = useState(false);

  const isLastStep = step === slides.length - 1;
  const slide = slides[step];

  return (
    <div className="consent-page">
      <div className="consent-card">
        <div className="consent-topbar">
          <span className="consent-logo">MedTrack</span>
          {!isLastStep && (
            <button type="button" className="consent-skip" onClick={() => setStep(slides.length - 1)}>
              Überspringen
            </button>
          )}
        </div>

        <div className="consent-icon">
          <Icon name={slide.icon} size={36} />
        </div>

        <h1>{slide.title}</h1>
        <p>{slide.body}</p>

        {isLastStep && (
          <label className="consent-checkbox">
            <input
              type="checkbox"
              checked={checked}
              onChange={(e) => setChecked(e.target.checked)}
            />
            <span>Ich habe verstanden, dass dies ein Demo-Projekt mit ausschließlich fiktiven Daten ist.</span>
          </label>
        )}

        <div className="consent-dots">
          {slides.map((_, i) => (
            <span key={i} className={"consent-dot" + (i === step ? " is-active" : "")} />
          ))}
        </div>

        {isLastStep ? (
          <button type="button" className="consent-cta" disabled={!checked} onClick={onAccept}>
            Jetzt MedTrack nutzen
          </button>
        ) : (
          <button type="button" className="consent-cta" onClick={() => setStep((s) => s + 1)}>
            Weiter
          </button>
        )}
      </div>
    </div>
  );
}
