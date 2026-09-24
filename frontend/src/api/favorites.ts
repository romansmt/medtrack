import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiSend } from "./client";
import type { Drug } from "./drugs";
import type { Pharmacy } from "./pharmacies";

export function useFavoritePharmaciesQuery(svnr: string | undefined) {
  return useQuery({
    queryKey: ["favorites", svnr, "pharmacies"],
    queryFn: () => apiGet<Pharmacy[]>(`/api/patients/${svnr}/favorite-pharmacies`),
    enabled: svnr !== undefined,
  });
}

export function useFavoriteDrugsQuery(svnr: string | undefined) {
  return useQuery({
    queryKey: ["favorites", svnr, "drugs"],
    queryFn: () => apiGet<Drug[]>(`/api/patients/${svnr}/favorite-drugs`),
    enabled: svnr !== undefined,
  });
}

export function useToggleFavoritePharmacyMutation(svnr: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ pharmacyId, isFavorite }: { pharmacyId: number; isFavorite: boolean }) =>
      apiSend<void>(isFavorite ? "DELETE" : "POST", `/api/patients/${svnr}/favorite-pharmacies/${pharmacyId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["favorites", svnr, "pharmacies"] });
    },
  });
}

export function useToggleFavoriteDrugMutation(svnr: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ drugId, isFavorite }: { drugId: number; isFavorite: boolean }) =>
      apiSend<void>(isFavorite ? "DELETE" : "POST", `/api/patients/${svnr}/favorite-drugs/${drugId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["favorites", svnr, "drugs"] });
    },
  });
}
