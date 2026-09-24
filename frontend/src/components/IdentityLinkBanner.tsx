import { Icon } from "../layout/Icon";
import "./IdentityLinkBanner.css";

export function IdentityLinkBanner({ onReopenRegistration }: { onReopenRegistration: () => void }) {
  return (
    <div className="identity-link-banner">
      <Icon name="shield" size={18} />
      <span>Sie nutzen MedTrack ohne ID-Austria-Verknüpfung.</span>
      <button type="button" onClick={onReopenRegistration}>
        ID Austria verknüpfen
      </button>
    </div>
  );
}
