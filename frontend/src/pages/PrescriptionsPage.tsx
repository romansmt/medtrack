import { usePrescriptionsQuery } from "../api/prescriptions";
import { usePatient } from "../context/PatientContext";
import { Icon } from "../layout/Icon";
import { formatDate } from "../utils/format";
import "./PrescriptionsPage.css";

export function PrescriptionsPage() {
  const { selectedPatient } = usePatient();
  const prescriptions = usePrescriptionsQuery(selectedPatient?.svnr);

  return (
    <section className="prescriptions-page">
      <h1>Meine Rezepte</h1>

      {prescriptions.isLoading && <p className="prescriptions-page__hint">Lade Rezepte…</p>}

      {prescriptions.data && prescriptions.data.length === 0 && (
        <div className="prescriptions-page__empty">
          <Icon name="doc" size={28} />
          <p>Keine Rezepte gefunden</p>
        </div>
      )}

      {prescriptions.data && prescriptions.data.length > 0 && (
        <ul className="prescriptions-page__list">
          {prescriptions.data.map((prescription, i) => (
            <li key={i} className="prescription-row">
              <div className="prescription-row__info">
                <span className="prescription-row__drug">{prescription.drug}</span>
                <span className="prescription-row__meta">{prescription.dosage}</span>
                <span className="prescription-row__meta">
                  {prescription.doctorName} · {formatDate(prescription.issuedDate)}
                </span>
              </div>
              <span
                className={
                  "prescription-row__status" +
                  (prescription.status === "OPEN"
                    ? " prescription-row__status--open"
                    : " prescription-row__status--redeemed")
                }
              >
                {prescription.status === "OPEN" ? "Offen" : "Eingelöst"}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
