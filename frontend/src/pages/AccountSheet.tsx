import { usePatient } from "../context/PatientContext";
import { Icon, type IconName } from "../layout/Icon";
import "./AccountSheet.css";

const PLACEHOLDER_ITEMS: { icon: IconName; label: string }[] = [
  { icon: "settings", label: "Einstellungen" },
  { icon: "feedback", label: "Feedback" },
  { icon: "info", label: "Info" },
  { icon: "help", label: "Hilfe" },
];

// The account menu (screenshot reference: ApoScout's "Mein Konto"). Reached from the header, never
// forced open - see App.tsx. Settings/Feedback/Info/Hilfe and the two footer links are deliberately
// inert placeholders: they show the expected structure of a real account menu without pretending
// this demo project has settings, a feedback pipeline, or real terms/privacy pages behind them.
export function AccountSheet({
  isRegistered,
  isLinked,
  isAdmin,
  onOpenAuth,
  onOpenAdminAccess,
  onDeactivateAdmin,
  onClose,
}: {
  isRegistered: boolean;
  isLinked: boolean;
  isAdmin: boolean;
  onOpenAuth: () => void;
  onOpenAdminAccess: () => void;
  onDeactivateAdmin: () => void;
  onClose: () => void;
}) {
  const { selectedPatient } = usePatient();

  return (
    <div className="account-sheet">
      <div className="account-sheet__topbar">
        <span className="account-sheet__title">Mein Konto</span>
        <button type="button" className="account-sheet__close" onClick={onClose} aria-label="Schließen">
          ×
        </button>
      </div>

      <div className="account-sheet__profile">
        <div className="account-sheet__avatar">
          <Icon name="user" size={26} />
        </div>
        {isRegistered && selectedPatient ? (
          <div className="account-sheet__identity">
            <span className="account-sheet__name">{selectedPatient.name}</span>
            <span className="account-sheet__medtrack-id">{selectedPatient.medtrackId}</span>
          </div>
        ) : (
          <button type="button" className="account-sheet__login-row" onClick={onOpenAuth}>
            Anmelden
          </button>
        )}
      </div>

      {isRegistered && (
        <button type="button" className="account-sheet__link-row" onClick={onOpenAuth}>
          <span className="account-sheet__link-row-label">
            <Icon name="shield" size={16} />
            {isLinked ? "Mit ID Austria verifiziert" : "ID Austria verknüpfen"}
          </span>
          {!isLinked && <Icon name="chevron" size={16} />}
        </button>
      )}

      <button type="button" className="account-sheet__link-row" onClick={isAdmin ? onDeactivateAdmin : onOpenAdminAccess}>
        <span className="account-sheet__link-row-label">
          <Icon name="settings" size={16} />
          {isAdmin ? "Admin-Modus aktiv - Tippen zum Beenden" : "Admin-Zugang"}
        </span>
        {!isAdmin && <Icon name="chevron" size={16} />}
      </button>

      <div className="account-sheet__list">
        {PLACEHOLDER_ITEMS.map((item) => (
          <div className="account-sheet__list-item" key={item.label} aria-disabled="true">
            <Icon name={item.icon} size={20} />
            <span>{item.label}</span>
          </div>
        ))}
      </div>

      <p className="account-sheet__footer">
        <span>Nutzungsbedingungen</span>
        <span>Datenschutzinformation</span>
      </p>
    </div>
  );
}
