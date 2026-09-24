import { useState } from "react";
import {
  useFavoriteDrugsQuery,
  useFavoritePharmaciesQuery,
  useToggleFavoriteDrugMutation,
  useToggleFavoritePharmacyMutation,
} from "../api/favorites";
import { usePatient } from "../context/PatientContext";
import { FavoriteButton } from "../components/FavoriteButton";
import { PharmacyCard } from "../components/PharmacyCard";
import { Icon } from "../layout/Icon";
import "./FavoritesPage.css";

export function FavoritesPage() {
  const { selectedPatient } = usePatient();
  const svnr = selectedPatient?.svnr;
  const [tab, setTab] = useState<"pharmacies" | "drugs">("pharmacies");

  const pharmacies = useFavoritePharmaciesQuery(svnr);
  const drugs = useFavoriteDrugsQuery(svnr);
  const togglePharmacy = useToggleFavoritePharmacyMutation(svnr);
  const toggleDrug = useToggleFavoriteDrugMutation(svnr);

  return (
    <section className="favorites-page">
      <h1>Favoriten</h1>

      <div className="favorites-page__tabs">
        <button type="button" className={tab === "pharmacies" ? "is-active" : ""} onClick={() => setTab("pharmacies")}>
          Apotheken
        </button>
        <button type="button" className={tab === "drugs" ? "is-active" : ""} onClick={() => setTab("drugs")}>
          Medikamente
        </button>
      </div>

      {tab === "pharmacies" &&
        (pharmacies.data && pharmacies.data.length > 0 ? (
          <div className="favorites-page__list">
            {pharmacies.data.map((pharmacy) => (
              <PharmacyCard
                key={pharmacy.id}
                pharmacy={pharmacy}
                isFavorite
                favoritePending={togglePharmacy.isPending}
                onToggleFavorite={() => togglePharmacy.mutate({ pharmacyId: pharmacy.id, isFavorite: true })}
              />
            ))}
          </div>
        ) : (
          <div className="favorites-page__empty">
            <Icon name="heart" size={28} />
            <p>Keine Stammapotheken gefunden</p>
          </div>
        ))}

      {tab === "drugs" &&
        (drugs.data && drugs.data.length > 0 ? (
          <ul className="favorites-page__drug-list">
            {drugs.data.map((drug) => (
              <li key={drug.id} className="favorite-drug-row">
                <div>
                  <span className="favorite-drug-row__name">{drug.name}</span>
                  <span className="favorite-drug-row__meta">
                    {drug.form}, {drug.packSize}
                  </span>
                </div>
                <FavoriteButton
                  isFavorite
                  pending={toggleDrug.isPending}
                  onToggle={() => toggleDrug.mutate({ drugId: drug.id, isFavorite: true })}
                />
              </li>
            ))}
          </ul>
        ) : (
          <div className="favorites-page__empty">
            <Icon name="heart" size={28} />
            <p>Keine Lieblingsmedikamente gefunden</p>
          </div>
        ))}
    </section>
  );
}
