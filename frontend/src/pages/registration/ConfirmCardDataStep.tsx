import { useState } from "react";
import type { ECardDetails } from "../../api/registration";
import { Icon } from "../../layout/Icon";

// Mirrors MockEHealthCardAdapter.dateOfBirthFromSvnr on the backend - re-derives the birthdate
// from whatever SVNR is currently in the form (not just the scan's original value), so editing the
// SVNR field is itself covered by the consistency check.
function birthDateFromSvnr(svnr: string): string | null {
  if (!/^\d{10}$/.test(svnr)) return null;
  const day = svnr.slice(4, 6);
  const month = svnr.slice(6, 8);
  const twoDigitYear = svnr.slice(8, 10);
  const currentYear = new Date().getFullYear();
  let fullYear = 2000 + Number(twoDigitYear);
  if (fullYear > currentYear) fullYear -= 100;
  return `${fullYear}-${month}-${day}`;
}

export function ConfirmCardDataStep({
  details,
  registeredDateOfBirth,
  onBack,
  onConfirm,
}: {
  details: ECardDetails;
  registeredDateOfBirth: string;
  onBack: () => void;
  onConfirm: (svnr: string) => void;
}) {
  const [svnr, setSvnr] = useState(details.svnr);
  const [firstName, setFirstName] = useState(details.firstName);
  const [lastName, setLastName] = useState(details.lastName);
  const [dateOfBirth, setDateOfBirth] = useState(details.dateOfBirth);
  const [cardSerialNumber, setCardSerialNumber] = useState(details.cardSerialNumber);
  const [carrierNumber, setCarrierNumber] = useState(details.carrierNumber);
  const [expiryDate, setExpiryDate] = useState(details.expiryDate);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const svnrBirthDate = birthDateFromSvnr(svnr);
    if (!svnrBirthDate) {
      setError("Die SVNR muss aus genau 10 Ziffern bestehen.");
      return;
    }
    if (svnrBirthDate !== registeredDateOfBirth) {
      setError(
        "Das in der SVNR enthaltene Geburtsdatum stimmt nicht mit dem bei der ID-Austria-Anmeldung " +
          "angegebenen Geburtsdatum überein. Bitte überprüfen Sie die eingegebenen Daten.",
      );
      return;
    }

    setError(null);
    onConfirm(svnr);
  };

  return (
    <form className="registration-step" onSubmit={handleSubmit}>
      <div className="registration-step__icon">
        <Icon name="doc" size={32} />
      </div>
      <h1>Daten überprüfen</h1>
      <p className="registration-step__intro">
        Bitte überprüfen und vervollständigen Sie die e-card-Daten, bevor Sie fortfahren.
      </p>

      <label>
        MedTrack-ID
        <input
          type="text"
          value={details.medtrackId || "(wird nach Bestätigung zugewiesen)"}
          readOnly
        />
      </label>
      <label>
        SVNR
        <input type="text" value={svnr} onChange={(e) => setSvnr(e.target.value)} maxLength={10} required />
      </label>
      <div className="registration-step__row">
        <label>
          Vorname
          <input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
        </label>
        <label>
          Nachname
          <input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
        </label>
      </div>
      <label>
        Geburtsdatum
        <input type="date" value={dateOfBirth} onChange={(e) => setDateOfBirth(e.target.value)} required />
      </label>
      <label>
        Kennnummer der Karte
        <input
          type="text"
          value={cardSerialNumber}
          onChange={(e) => setCardSerialNumber(e.target.value)}
          maxLength={20}
          required
        />
      </label>
      <div className="registration-step__row">
        <label>
          Kennnummer des Trägers
          <input
            type="text"
            value={carrierNumber}
            onChange={(e) => setCarrierNumber(e.target.value)}
            maxLength={4}
            required
          />
        </label>
        <label>
          Versicherung
          <input type="text" value={details.carrierName} readOnly />
        </label>
      </div>
      <label>
        Ablaufdatum
        <input type="date" value={expiryDate} onChange={(e) => setExpiryDate(e.target.value)} required />
      </label>

      {error && <p className="registration-step__error">{error}</p>}

      <button type="submit" className="registration-step__cta">
        Bestätigen
      </button>
      <button type="button" className="registration-step__back" onClick={onBack}>
        Zurück
      </button>
    </form>
  );
}
