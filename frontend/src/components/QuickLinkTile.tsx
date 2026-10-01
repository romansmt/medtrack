import { Link } from "react-router-dom";
import { Icon, type IconName } from "../layout/Icon";
import "./QuickLinkTile.css";

export function QuickLinkTile({ to, icon, label }: { to: string; icon: IconName; label: string }) {
  return (
    <Link to={to} className="quick-link-tile">
      <span className="quick-link-tile__icon">
        <Icon name={icon} size={20} />
      </span>
      <span>{label}</span>
    </Link>
  );
}
