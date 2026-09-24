import type { OpeningHours } from "../api/pharmacies";
import { formatTime, germanDayName } from "../utils/format";
import "./PharmacyHours.css";

export function PharmacyHours({ hours }: { hours: OpeningHours[] }) {
  const regular = hours.filter((h) => h.kind === "REGULAR");
  const onCall = hours.filter((h) => h.kind === "ON_CALL");

  return (
    <div className="pharmacy-hours">
      <dl className="pharmacy-hours__list">
        {regular.map((h) => (
          <div key={h.dayOfWeek} className="pharmacy-hours__row">
            <dt>{germanDayName(h.dayOfWeek)}</dt>
            <dd>
              {formatTime(h.opensAt)} - {formatTime(h.closesAt)}
            </dd>
          </div>
        ))}
      </dl>

      {onCall.length > 0 && (
        <>
          <h4>Bereitschaftszeiten</h4>
          <dl className="pharmacy-hours__list">
            {onCall.map((h) => (
              <div key={h.dayOfWeek} className="pharmacy-hours__row">
                <dt>{germanDayName(h.dayOfWeek)}</dt>
                <dd>
                  {formatTime(h.opensAt)} - {formatTime(h.closesAt)}
                </dd>
              </div>
            ))}
          </dl>
        </>
      )}
    </div>
  );
}
