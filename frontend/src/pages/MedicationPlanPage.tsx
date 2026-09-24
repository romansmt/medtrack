import { useState } from "react";
import type { Drug } from "../api/drugs";
import {
  useCreateScheduleMutation,
  useDeactivateScheduleMutation,
  useDueTodayQuery,
  useMedicationSchedulesQuery,
  useRecordIntakeMutation,
} from "../api/medicationSchedule";
import { usePatient } from "../context/PatientContext";
import { DrugSearchCombobox } from "../components/DrugSearchCombobox";
import { Icon } from "../layout/Icon";
import { formatDate, formatTime } from "../utils/format";
import "./MedicationPlanPage.css";

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function AddScheduleForm({ onClose, onSubmit, isSubmitting }: {
  onClose: () => void;
  onSubmit: (input: { drug: Drug; doseText: string; startDate: string; endDate: string | null; times: string[] }) => void;
  isSubmitting: boolean;
}) {
  const [drug, setDrug] = useState<Drug | null>(null);
  const [doseText, setDoseText] = useState("");
  const [startDate, setStartDate] = useState(todayIso());
  const [endDate, setEndDate] = useState("");
  const [times, setTimes] = useState<string[]>(["08:00"]);

  const canSubmit = drug !== null && doseText.trim() !== "" && times.length > 0;

  return (
    <form
      className="add-schedule-form"
      onSubmit={(e) => {
        e.preventDefault();
        if (!drug) return;
        onSubmit({ drug, doseText: doseText.trim(), startDate, endDate: endDate || null, times });
      }}
    >
      <h2>Medikament hinzufügen</h2>

      {drug ? (
        <div className="add-schedule-form__selected-drug">
          <span>{drug.name}</span>
          <button type="button" onClick={() => setDrug(null)}>
            Ändern
          </button>
        </div>
      ) : (
        <DrugSearchCombobox onSelect={setDrug} />
      )}

      <label>
        Dosierung
        <input
          type="text"
          value={doseText}
          onChange={(e) => setDoseText(e.target.value)}
          placeholder="z. B. 1 Tablette"
        />
      </label>

      <div className="add-schedule-form__dates">
        <label>
          Start
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} required />
        </label>
        <label>
          Ende (optional)
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </label>
      </div>

      <div className="add-schedule-form__times">
        <span>Uhrzeiten</span>
        {times.map((time, i) => (
          <div key={i} className="add-schedule-form__time-row">
            <input
              type="time"
              value={time}
              onChange={(e) => setTimes((prev) => prev.map((t, idx) => (idx === i ? e.target.value : t)))}
            />
            {times.length > 1 && (
              <button type="button" onClick={() => setTimes((prev) => prev.filter((_, idx) => idx !== i))}>
                Entfernen
              </button>
            )}
          </div>
        ))}
        <button type="button" onClick={() => setTimes((prev) => [...prev, "08:00"])}>
          + Uhrzeit hinzufügen
        </button>
      </div>

      <div className="add-schedule-form__actions">
        <button type="button" onClick={onClose}>
          Abbrechen
        </button>
        <button type="submit" disabled={!canSubmit || isSubmitting}>
          {isSubmitting ? "Speichere…" : "Speichern"}
        </button>
      </div>
    </form>
  );
}

export function MedicationPlanPage() {
  const { selectedPatient } = usePatient();
  const svnr = selectedPatient?.svnr;
  const schedules = useMedicationSchedulesQuery(svnr);
  const dueToday = useDueTodayQuery(svnr);
  const createSchedule = useCreateScheduleMutation(svnr);
  const deactivateSchedule = useDeactivateScheduleMutation(svnr);
  const recordIntake = useRecordIntakeMutation(svnr);
  const [showAddForm, setShowAddForm] = useState(false);

  const activeSchedules = (schedules.data ?? []).filter((s) => s.active);

  return (
    <section className="medication-plan-page">
      <div className="medication-plan-page__header">
        <h1>Einnahmeplan</h1>
      </div>

      {dueToday.data && dueToday.data.length > 0 && (
        <div className="medication-plan-page__due-today">
          <h2>Heute fällig</h2>
          <ul>
            {dueToday.data.map((due) => (
              <li key={due.scheduleTimeId} className="due-row">
                <div className="due-row__info">
                  <span className="due-row__name">{due.drugName}</span>
                  <span className="due-row__meta">
                    {due.doseText} · {formatTime(due.timeOfDay)}
                  </span>
                </div>
                {due.status === "PENDING" ? (
                  <div className="due-row__actions">
                    <button
                      type="button"
                      className="due-row__confirm"
                      onClick={() => recordIntake.mutate({ scheduleTimeId: due.scheduleTimeId, status: "TAKEN" })}
                      disabled={recordIntake.isPending}
                    >
                      Einnahme bestätigen
                    </button>
                    <button
                      type="button"
                      className="due-row__skip"
                      onClick={() => recordIntake.mutate({ scheduleTimeId: due.scheduleTimeId, status: "SKIPPED" })}
                      disabled={recordIntake.isPending}
                    >
                      Überspringen
                    </button>
                  </div>
                ) : (
                  <span className={due.status === "TAKEN" ? "due-row__done" : "due-row__skipped"}>
                    {due.status === "TAKEN" ? "Eingenommen" : "Übersprungen"}
                  </span>
                )}
              </li>
            ))}
          </ul>
        </div>
      )}

      {!schedules.isLoading && activeSchedules.length === 0 && !showAddForm && (
        <div className="medication-plan-page__empty">
          <div className="medication-plan-page__empty-icon">
            <Icon name="pill" size={32} />
          </div>
          <h2>Noch keine Medikamente eingetragen</h2>
          <p>Fügen Sie Ihr erstes Medikament hinzu und behalten Sie Ihre Einnahme im Blick.</p>
          <button type="button" className="medication-plan-page__add-button" onClick={() => setShowAddForm(true)}>
            + Medikament hinzufügen
          </button>
        </div>
      )}

      {activeSchedules.length > 0 && (
        <div className="medication-plan-page__schedules">
          <h2>Meine Medikamente</h2>
          <ul>
            {activeSchedules.map((schedule) => (
              <li key={schedule.id} className="schedule-row">
                <div className="schedule-row__info">
                  <span className="schedule-row__name">{schedule.drugName}</span>
                  <span className="schedule-row__meta">
                    {schedule.doseText} · {schedule.times.map((t) => formatTime(t.timeOfDay)).join(", ")}
                  </span>
                  <span className="schedule-row__dates">
                    seit {formatDate(schedule.startDate)}
                    {schedule.endDate && ` bis ${formatDate(schedule.endDate)}`}
                  </span>
                </div>
                <button
                  type="button"
                  className="schedule-row__stop"
                  onClick={() => deactivateSchedule.mutate(schedule.id)}
                  disabled={deactivateSchedule.isPending}
                >
                  Stoppen
                </button>
              </li>
            ))}
          </ul>
          {!showAddForm && (
            <button type="button" className="medication-plan-page__add-button" onClick={() => setShowAddForm(true)}>
              + Medikament hinzufügen
            </button>
          )}
        </div>
      )}

      {showAddForm && (
        <AddScheduleForm
          isSubmitting={createSchedule.isPending}
          onClose={() => setShowAddForm(false)}
          onSubmit={(input) =>
            createSchedule.mutate(
              {
                drugId: input.drug.id,
                doseText: input.doseText,
                startDate: input.startDate,
                endDate: input.endDate,
                times: input.times,
              },
              { onSuccess: () => setShowAddForm(false) },
            )
          }
        />
      )}
    </section>
  );
}
