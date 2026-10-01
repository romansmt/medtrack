import { Link } from "react-router-dom";
import { Icon } from "../layout/Icon";
import "./PopularMedicationsRow.css";

// A curated shortlist, not a real "trending" ranking - there's no popularity data to back one, so
// fabricating price/trend numbers would just be misleading. Each card hands off to the existing
// search flow (same ?q= param SearchBar itself uses) rather than duplicating availability logic.
const popularDrugs = ["Aspirin", "Paracetamol", "Nurofen", "Omeprazole", "Amoxicillin", "Metformin"] as const;

export function PopularMedicationsRow() {
  return (
    <section className="popular-row">
      <div className="section-header">
        <h2>Beliebte Medikamente</h2>
      </div>
      <div className="scroll-row">
        {popularDrugs.map((name) => (
          <Link key={name} to={`/search?q=${encodeURIComponent(name)}`} className="popular-card">
            <span className="popular-card__icon">
              <Icon name="pill" size={20} />
            </span>
            <span className="popular-card__name">{name}</span>
            <span className="popular-card__cta">Verfügbarkeit prüfen</span>
          </Link>
        ))}
      </div>
    </section>
  );
}
