import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiSend } from "./client";

export interface ScheduleTime {
  id: number;
  timeOfDay: string;
}

export interface MedicationSchedule {
  id: number;
  drugId: number;
  drugName: string;
  doseText: string;
  startDate: string;
  endDate: string | null;
  active: boolean;
  times: ScheduleTime[];
}

export type IntakeStatus = "PENDING" | "TAKEN" | "SKIPPED";

export interface DueMedication {
  scheduleId: number;
  scheduleTimeId: number;
  drugName: string;
  doseText: string;
  timeOfDay: string;
  scheduledDate: string;
  status: IntakeStatus;
  confirmedAt: string | null;
}

export interface CreateScheduleRequest {
  drugId: number;
  doseText: string;
  startDate: string;
  endDate: string | null;
  times: string[];
}

function scheduleKey(svnr: string | undefined) {
  return ["medication-schedule", svnr] as const;
}

function dueTodayKey(svnr: string | undefined) {
  return ["medication-schedule", svnr, "due-today"] as const;
}

export function useMedicationSchedulesQuery(svnr: string | undefined) {
  return useQuery({
    queryKey: scheduleKey(svnr),
    queryFn: () => apiGet<MedicationSchedule[]>(`/api/patients/${svnr}/medication-schedule`),
    enabled: svnr !== undefined,
  });
}

export function useDueTodayQuery(svnr: string | undefined) {
  return useQuery({
    queryKey: dueTodayKey(svnr),
    queryFn: () => apiGet<DueMedication[]>(`/api/patients/${svnr}/medication-schedule/due-today`),
    enabled: svnr !== undefined,
  });
}

export function useCreateScheduleMutation(svnr: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateScheduleRequest) =>
      apiSend<MedicationSchedule>("POST", `/api/patients/${svnr}/medication-schedule`, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: scheduleKey(svnr) });
      queryClient.invalidateQueries({ queryKey: dueTodayKey(svnr) });
    },
  });
}

export function useDeactivateScheduleMutation(svnr: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (scheduleId: number) =>
      apiSend<void>("DELETE", `/api/patients/${svnr}/medication-schedule/${scheduleId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: scheduleKey(svnr) });
      queryClient.invalidateQueries({ queryKey: dueTodayKey(svnr) });
    },
  });
}

export function useRecordIntakeMutation(svnr: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ scheduleTimeId, status }: { scheduleTimeId: number; status: "TAKEN" | "SKIPPED" }) =>
      apiSend<DueMedication>("POST", `/api/patients/${svnr}/medication-schedule/${scheduleTimeId}/intake`, {
        status,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dueTodayKey(svnr) });
    },
  });
}
