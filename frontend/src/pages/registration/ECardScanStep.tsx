import { useEffect, useState } from "react";
import { ApiError } from "../../api/client";
import { useScanCardMutation, type ECardDetails } from "../../api/registration";
import { Icon } from "../../layout/Icon";

const SCAN_DELAY_MS = 1200;

export function ECardScanStep({
  fullName,
  onBack,
  onSuccess,
  onManualEntry,
}: {
  fullName: string;
  onBack: () => void;
  onSuccess: (details: ECardDetails) => void;
  onManualEntry: () => void;
}) {
  const [file, setFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [notFound, setNotFound] = useState(false);
  const scanCard = useScanCardMutation();

  // Revoke the previous preview URL whenever the file changes (or the step unmounts), so
  // repeatedly picking a new photo doesn't leak object URLs.
  useEffect(() => {
    if (!file) {
      setPreviewUrl(null);
      return;
    }
    const url = URL.createObjectURL(file);
    setPreviewUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  const choosePhoto = (selected: File | null) => {
    setFile(selected);
    setNotFound(false);
  };

  const handleScan = () => {
    if (!file) return;
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

      {!file && (
        <label
          className={"registration-step__file-drop" + (isDragging ? " is-dragging" : "")}
          onDragOver={(e) => {
            e.preventDefault();
            setIsDragging(true);
          }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={(e) => {
            e.preventDefault();
            setIsDragging(false);
            choosePhoto(e.dataTransfer.files?.[0] ?? null);
          }}
        >
          <Icon name="camera" size={24} />
          <span>Foto aufnehmen, hierher ziehen oder Datei auswählen</span>
          <input
            type="file"
            accept="image/*"
            capture="environment"
            onClick={(e) => (e.currentTarget.value = "")}
            onChange={(e) => choosePhoto(e.target.files?.[0] ?? null)}
            hidden
          />
        </label>
      )}

      {file && (
        <div className="registration-step__preview">
          {previewUrl && <img src={previewUrl} alt="Vorschau der e-card" />}
          <span className="registration-step__filename">{file.name}</span>
        </div>
      )}

      {scanning && <p className="registration-step__status">Scanne e-card…</p>}

      {notFound && (
        <p className="registration-step__error">
          Für "{fullName}" wurde keine e-card gefunden. Dieses Demo-Projekt kennt nur die vorhandenen
          Demo-Patienten: Anna Gruber, Max Bauer, Lena Hofer, Paul Wagner. Wählen Sie ein anderes Foto,
          gehen Sie zurück und prüfen Sie den bei der Anmeldung eingegebenen Namen, oder geben Sie die
          Werte manuell ein.
        </p>
      )}

      {file && !scanning && (
        <div className="registration-step__row">
          <button type="button" className="registration-step__secondary" onClick={() => choosePhoto(null)}>
            Anderes Foto wählen
          </button>
          <button type="button" className="registration-step__cta" onClick={handleScan}>
            Foto scannen
          </button>
        </div>
      )}

      {!scanning && (
        <button type="button" className="registration-step__manual-entry" onClick={onManualEntry}>
          Kein Foto zur Hand? Werte stattdessen manuell eingeben
        </button>
      )}

      <button type="button" className="registration-step__back" onClick={onBack}>
        Zurück
      </button>
    </div>
  );
}
