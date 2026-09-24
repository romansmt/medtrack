import { Icon } from "../layout/Icon";
import "./FavoriteButton.css";

export function FavoriteButton({
  isFavorite,
  onToggle,
  pending,
}: {
  isFavorite: boolean;
  onToggle: () => void;
  pending?: boolean;
}) {
  return (
    <button
      type="button"
      className={"favorite-button" + (isFavorite ? " is-active" : "")}
      onClick={(e) => {
        e.preventDefault();
        e.stopPropagation();
        onToggle();
      }}
      disabled={pending}
      aria-pressed={isFavorite}
      aria-label={isFavorite ? "Aus Favoriten entfernen" : "Zu Favoriten hinzufügen"}
    >
      <Icon name="heart" size={20} />
    </button>
  );
}
