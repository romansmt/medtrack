import { useCancelReservationMutation, useReservationsQuery } from "../api/reservations";
import { usePatient } from "../context/PatientContext";
import { Icon } from "../layout/Icon";
import { formatDateTime } from "../utils/format";
import "./ReservationsPage.css";

export function ReservationsPage() {
  const { selectedPatient } = usePatient();
  const svnr = selectedPatient?.svnr;
  const reservations = useReservationsQuery(svnr);
  const cancel = useCancelReservationMutation(svnr);

  const active = (reservations.data ?? []).filter((r) => r.status === "REQUESTED");

  return (
    <section className="reservations-page">
      <h1>Reservierungen</h1>

      {reservations.data && active.length === 0 && (
        <div className="reservations-page__empty">
          <Icon name="bookmark" size={28} />
          <p>Keine Reservierungen</p>
        </div>
      )}

      {active.length > 0 && (
        <ul className="reservations-page__list">
          {active.map((reservation) => (
            <li key={reservation.id} className="reservation-row">
              <div className="reservation-row__info">
                <span className="reservation-row__name">
                  {reservation.drugName} × {reservation.quantity}
                </span>
                <span className="reservation-row__meta">bei {reservation.pharmacyName}</span>
                <span className="reservation-row__meta">angefragt {formatDateTime(reservation.requestedAt)}</span>
              </div>
              <button
                type="button"
                className="reservation-row__cancel"
                onClick={() => cancel.mutate(reservation.id)}
                disabled={cancel.isPending}
              >
                Stornieren
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
