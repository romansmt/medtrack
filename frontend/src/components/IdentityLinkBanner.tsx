import { Icon } from "../layout/Icon";
import "./IdentityLinkBanner.css";

export function IdentityLinkBanner({ onOpenAuthModal }: { onOpenAuthModal: () => void }) {
  return (
    <div className="identity-link-banner">
      <Icon name="shield" size={18} />
      <span>Sie nutzen MedTrack ohne ID-Austria-Verknüpfung.</span>
      <button type="button" onClick={onOpenAuthModal}>
        ID Austria verknüpfen
      </button>
    </div>
  );
}
