import { useState } from "react";
import { ApiError } from "../../api/client";
import { useScanCardMutation, type ECardDetails } from "../../api/registration";
import { Icon } from "../../layout/Icon";

const SCAN_DELAY_MS = 1200;

export function ECardScanStep({
  fullName,
  onBack,
  onSuccess,
}: {
  fullName: string;
  onBack: () => void;
  onSuccess: (details: ECardDetails) => void;
}) {
  const [fileName, setFileName] = useState<string | null>(null);
  const [scanning, setScanning] = useState(false);
  const [notFound, setNotFound] = useState(false);
  const scanCard = useScanCardMutation();

  const handleFileSelected = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setFileName(file.name);
    setNotFound(false);
    setScanning(true);

    setTimeout(() => {
      scanCard.mutate(fullName, {
        onSuccess: (details) => {
          setScanning(false);
          onSuccess(details);
        },
        onError: (error) => {
          setScanning(false);
          if (error instanceof ApiError && error.status === 404) {
            setNotFound(true);
          }
        },
      });
    }, SCAN_DELAY_MS);
  };

  return (
    <div className="registration-step">
      <div className="registration-step__icon">
        <Icon name="camera" size={32} />
      </div>
      <h1>e-card fotografieren</h1>
      <p className="registration-step__intro">
        Fotografieren Sie die Vorderseite Ihrer e-card, um Ihre Daten zu übernehmen. In diesem
        Demo-Projekt wird das Foto nicht wirklich ausgewertet - der Name aus der ID-Austria-Anmeldung
        wird zur Simulation verwendet.
      </p>

      <label className="registration-step__file-drop">
        <Icon name="camera" size={24} />
        <span>{fileName ?? "Foto aufnehmen oder Datei auswählen"}</span>
        <input type="file" accept="image/*" capture="environment" onChange={handleFileSelected} hidden />
      </label>

      {scanning && <p className="registration-step__status">Scanne e-card…</p>}

      {notFound && (
        <p className="registration-step__error">
          Für "{fullName}" wurde keine e-card gefunden. Dieses Demo-Projekt kennt nur die vorhandenen
          Demo-Patienten: Anna Gruber, Max Bauer, Lena Hofer, Paul Wagner. Gehen Sie zurück und prüfen
          Sie den bei der Anmeldung eingegebenen Namen.
        </p>
      )}

      <button type="button" className="registration-step__back" onClick={onBack}>
        Zurück
      </button>
    </div>
  );
}
