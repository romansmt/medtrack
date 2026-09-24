import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiSend } from "./client";

export interface Reservation {
  id: number;
  pharmacyId: number;
  pharmacyName: string;
  drugId: number;
  drugName: string;
  quantity: number;
  status: "REQUESTED" | "CANCELLED";
  requestedAt: string;
}

export interface CreateReservationRequest {
  pharmacyId: number;
  drugId: number;
  quantity: number;
}

function reservationsKey(svnr: string | undefined) {
  return ["reservations", svnr] as const;
}

export function useReservationsQuery(svnr: string | undefined) {
  return useQuery({
    queryKey: reservationsKey(svnr),
    queryFn: () => apiGet<Reservation[]>(`/api/patients/${svnr}/reservations`),
    enabled: svnr !== undefined,
  });
}

export function useCreateReservationMutation(svnr: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateReservationRequest) =>
      apiSend<Reservation>("POST", `/api/patients/${svnr}/reservations`, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: reservationsKey(svnr) });
    },
  });
}

export function useCancelReservationMutation(svnr: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (reservationId: number) =>
      apiSend<void>("DELETE", `/api/patients/${svnr}/reservations/${reservationId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: reservationsKey(svnr) });
    },
  });
}
